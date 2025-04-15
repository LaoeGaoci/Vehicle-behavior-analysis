import os
import json

import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import matplotlib.image as mpimg
from matplotlib.offsetbox import OffsetImage, AnnotationBbox
from scipy.spatial import KDTree
from pykalman import KalmanFilter
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF, ConstantKernel as C
from sklearn.preprocessing import StandardScaler

# 设置文件夹路径
json_folder = "../data/video_data/"
output_folder = "../data/animation/test"

# 确保输出目录存在
os.makedirs(output_folder, exist_ok=True)

# 读取车辆图像
motorcycle_img = mpimg.imread('../image/car_class/motorcycle.png')
auto_img = mpimg.imread('../image/car_class/auto.png')
truck_img = mpimg.imread('../image/car_class/truck.png')
# 读取第一辆车替换图像(第一辆车默认变道车)
first_motorcycle_img = mpimg.imread('../image/car_class/first_motorcycle.png')
first_auto_img = mpimg.imread('../image/car_class/first_auto.png')
first_truck_img = mpimg.imread('../image/car_class/first_truck.png')

# 单位换算因子(英尺转米)
FEET_TO_METERS = 0.3048

# 设定全屏幕绘制的场景范围
SCREEN_X_MIN, SCREEN_X_MAX = 0, 10
SCREEN_Y_MIN, SCREEN_Y_MAX = -50, 400

# 指定要读取的 JSON 文件名称
specified_json_file = "i-80~1620.json"
json_path = os.path.join(json_folder, specified_json_file)

# 读取指定的 JSON 文件
with open(json_path, "r") as f:
    data = json.load(f)

# 提取车辆数据
vehicles = data.get("vehicleDataList", [])
vehicle_paths = []

for vehicle in vehicles:
    vehicle_id = vehicle["vehicleId"]
    v_class = vehicle.get("vClass", "N/A")

    if "path" in vehicle and "frame" in vehicle:
        for i, point in enumerate(vehicle["path"]):
            if i < len(vehicle["frame"]):
                frame = vehicle["frame"][i]
                vehicle_paths.append({
                    "vehicleId": vehicle_id,
                    "vClass": v_class,
                    "localX": point["localX"] * FEET_TO_METERS,  # X 轴转换为米
                    "localY": point["localY"] * FEET_TO_METERS,  # Y 轴转换为米
                    "laneId": frame["laneId"],
                    "velocity": frame["velocity"] * FEET_TO_METERS  # 速度转换为米/秒
                })

if not vehicle_paths:
    raise ValueError(f"⚠ 指定文件 {specified_json_file} 中没有轨迹数据")

df_paths = pd.DataFrame(vehicle_paths)
unique_vehicles = df_paths["vehicleId"].unique()

# 获取数据的 Y 轴最小、最大范围
data_y_min, data_y_max = df_paths["localY"].min(), df_paths["localY"].max()

# 计算 Y 轴缩放因子
scale_y = (SCREEN_Y_MAX - SCREEN_Y_MIN) / (data_y_max - data_y_min)
scale_x = 0.4  # 设定 X 轴缩放倍数
df_paths["localX"] = df_paths["localX"] * scale_x  # 应用 X 轴缩放

# 仅对 Y 轴进行等比缩放
df_paths["localY"] = (df_paths["localY"] - data_y_min) * scale_y + SCREEN_Y_MIN

# 卡尔曼滤波器函数
def kalman_smoothing(y):
    """ 使用卡尔曼滤波器对轨迹进行平滑 """
    kf = KalmanFilter(initial_state_mean=[y[0], 0], n_dim_obs=1, n_dim_state=2)
    kf.transition_matrices = [[1, 1], [0, 1]]
    kf.observation_matrices = [[1, 0]]
    smoothed_state_means, _ = kf.smooth(y.reshape(-1, 1))
    return smoothed_state_means[:, 0]

# 高斯过程回归（GPR）平滑函数
def gpr_smoothing(x, y):
    """ 使用高斯过程回归进行轨迹平滑 """
    x = x.reshape(-1, 1)
    scaler_x = StandardScaler()
    scaler_y = StandardScaler()
    x_scaled = scaler_x.fit_transform(x)
    y_scaled = scaler_y.fit_transform(y.reshape(-1, 1)).flatten()

    kernel = C(1.0, (1e-2, 1e3)) * RBF(length_scale=10, length_scale_bounds=(1, 100))
    gpr = GaussianProcessRegressor(kernel=kernel, n_restarts_optimizer=30)
    gpr.fit(x_scaled, y_scaled)

    y_pred_scaled = gpr.predict(x_scaled)
    return scaler_y.inverse_transform(y_pred_scaled.reshape(-1, 1)).flatten()

# 先使用卡尔曼滤波进行平滑处理
for vid in unique_vehicles:
    mask = df_paths["vehicleId"] == vid
    if len(df_paths[mask]) > 3:
        df_paths.loc[mask, "localX"] = kalman_smoothing(df_paths.loc[mask, "localX"].values)
        df_paths.loc[mask, "localY"] = kalman_smoothing(df_paths.loc[mask, "localY"].values)

# 再使用 GPR 进行更精细的平滑处理
for vid in unique_vehicles:
    mask = df_paths["vehicleId"] == vid
    if len(df_paths[mask]) > 3:
        df_paths.loc[mask, "localX"] = gpr_smoothing(df_paths.loc[mask, "localX"].index.values, df_paths.loc[mask, "localX"].values)
        df_paths.loc[mask, "localY"] = gpr_smoothing(df_paths.loc[mask, "localY"].index.values, df_paths.loc[mask, "localY"].values)

# 获取第一辆车的 ID
first_vehicle_id = unique_vehicles[0] if len(unique_vehicles) > 0 else None

fig, ax = plt.subplots(figsize=(5, 15), facecolor="gray")
ax.set_facecolor("gray")
ax.set_xlim(SCREEN_X_MIN, SCREEN_X_MAX)
ax.set_ylim(SCREEN_Y_MIN, SCREEN_Y_MAX)

# 绘制车道虚线（计算车道宽度）
lane_width = (13 * FEET_TO_METERS) / 3
for x in range(int(SCREEN_X_MIN), int(SCREEN_X_MAX), int(lane_width)):
    ax.plot([x, x], [SCREEN_Y_MIN, SCREEN_Y_MAX], color='white', linestyle='--', linewidth=1)

vehicle_icons = {}
first_vehicle_rect = None

for vid in unique_vehicles:
    v_class = df_paths[df_paths["vehicleId"] == vid]["vClass"].iloc[0]
    if v_class == 1:
        img = motorcycle_img
        zoom = 0.1
    elif v_class == 2:
        img = auto_img
        zoom = 0.07
    else:
        img = truck_img
        zoom = 0.13

    imagebox = OffsetImage(img, zoom=zoom)
    first_pos = df_paths[df_paths["vehicleId"] == vid][["localX", "localY"]].iloc[0]
    ab = AnnotationBbox(imagebox, (first_pos["localX"], first_pos["localY"]), frameon=False)
    ax.add_artist(ab)
    vehicle_icons[vid] = ab

    # 变道车区分（第一辆车特殊处理）
    if vid == first_vehicle_id:
        if v_class == 1:
            img = motorcycle_img
            zoom = 0.1
        elif v_class == 2:
            img = auto_img
            zoom = 0.07
        else:
            img = truck_img
            zoom = 0.13

text_info = ax.text(0.02, 0.98, "", transform=ax.transAxes, fontsize=10, verticalalignment='top')

def update(frame):
    info_text = ""
    positions = []
    y_offset_step = 2

    for vid in unique_vehicles:
        data = df_paths[df_paths["vehicleId"] == vid].iloc[:frame + 1]
        if not data.empty:
            last_data = data.iloc[-1]
            new_x, new_y = last_data["localX"], last_data["localY"]
            lane_id = last_data["laneId"]

            if positions:
                tree = KDTree(positions)
                distances, indexes = tree.query([new_x, new_y], k=1)
                if distances < 3:
                    new_y += y_offset_step

            positions.append((new_x, new_y))
            vehicle_icons[vid].xybox = (new_x, new_y)

            if vid == first_vehicle_id:
                v_class = df_paths[df_paths["vehicleId"] == vid]["vClass"].iloc[0]
                if v_class == 1:
                    img = first_motorcycle_img
                    zoom = 0.1
                elif v_class == 2:
                    img = first_auto_img
                    zoom = 0.07
                else:
                    img = first_truck_img
                    zoom = 0.13

                ab = vehicle_icons.get(vid)
                if ab:
                    ab.remove()

                imagebox = OffsetImage(img, zoom=zoom)
                ab = AnnotationBbox(imagebox, (new_x, new_y), frameon=False)
                vehicle_icons[vid] = ab
                ax.add_artist(ab)

            info_text += f"Vehicle {vid}: X={new_x:.2f}, Y={new_y:.2f}\n"

    text_info.set_text(info_text)
    return list(vehicle_icons.values()) + [text_info]

ani = animation.FuncAnimation(fig, update, frames=len(df_paths) // len(unique_vehicles), interval=50, blit=True)

# 存储视频，输出文件名与指定的 JSON 文件同名
output_filename = os.path.join(output_folder, specified_json_file.replace(".json", ".mp4"))
ani.save(output_filename, writer="ffmpeg", fps=20)

print(f"✅ {output_filename} 生成成功！")
