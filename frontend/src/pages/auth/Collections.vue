<script setup>
import {onMounted, ref, toRaw} from 'vue'
import {getAccount, getToken} from '@/router/index.js'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import axios from 'axios'
import {FilterMatchMode} from 'primevue/api'
import {useToast} from 'primevue/usetoast'

const value = ref('地点')
const options = ref(['地点', '路线', '常用地址'])

let account = getAccount()

let commonAddress = ref([])
const hideDialog = () => {
  AddressDialog.value = false
}
//修改常用地址
let addresses = ref('')
const saveProduct = () => {
  commonAddress.value = commonAddress.value.filter(
      (val) => val.user_address_id !== address.value.user_address_id
  )
  commonAddress.value.push(address.value)
  AddressDialog.value = false
  toast.add({severity: 'success', summary: '成功', detail: '常用地址已被更改!', life: 3000})
  addresses.value = toRaw(address.value)
  console.log(addresses.value.user_address)

  try {
    const response = axios.put(
        'http://localhost:8080/user/modifyInfo',
        {
          user_address_id: addresses.value.user_address_id,
          user_comment: addresses.value.user_comment,
          user_address: addresses.value.user_address,
          user_address_x: addresses.value.user_address_x,
          user_address_y: addresses.value.user_address_y,
          user_address_info: addresses.value.user_address_info
        },
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    console.log('保存成功')
    console.log(response)
  } catch (error) {
    console.error(error)
  }

  address.value = {}
}

const toast = useToast()
const dt = ref()
const deleteProductDialog = ref(false)

const products = ref([])
const address = ref()
const product = ref({})

const selectedProducts = ref()
const filters = ref({
  global: {value: null, matchMode: FilterMatchMode.CONTAINS}
})
const confirmDeleteProduct = (prod) => {
  product.value = prod
  deleteProductDialog.value = true
}
const deleteProduct = () => {
  products.value = products.value.filter(
      (val) => val.collection_location_id !== product.value.collection_location_id
  )
  try {
    const response = axios.delete(
        'http://localhost:8080/collections/poi/delete',
        {
          data:{
            collection_location_id: product.value.collection_location_id
          },
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    console.log('删除成功')
    console.log(response)
  } catch (error) {
    console.error(error)
  }
  deleteProductDialog.value = false
  product.value = {}
  toast.add({severity: 'success', summary: '成功', detail: '路线已被删除!', life: 3000})
}

const confirmDeleteAddress = (prod) => {
  address.value = prod
  deleteAddressDialog.value = true
}
let AddressDialog = ref(false)
const editProduct = (prod) => {
  address.value = {...prod}
  AddressDialog.value = true
}

const deleteAddress = () => {
  commonAddress.value = commonAddress.value.filter(
      (val) => val.user_address_id !== address.value.user_address_id
  )
  addresses.value = toRaw(address.value)
  console.log(addresses)
  try {
    const response = axios.delete(
        'http://localhost:8080/user/deleteAddress',
        {
          data:{
            user_address_id: addresses.value.user_address_id
          },
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    console.log('删除成功')
    console.log(response)
  } catch (error) {
    console.error(error)
  }
  address.value = {}
  deleteAddressDialog.value = false
  toast.add({severity: 'success', summary: '成功', detail: '常用地址已被删除!', life: 3000})

}

const exportCSV = () => {
  dt.value.exportCSV()
}
//

//===
const deletePathDialog = ref(false)
const deleteAddressDialog = ref(false)
let pathCollection = ref({})
const selectedPaths = ref()
const selectedCommonAddress = ref()
let pathCollections = ref([

])

const confirmDeletePath = (prod) => {
  pathCollection.value = prod
  deletePathDialog.value = true
}

const deletePath = () => {
  pathCollections.value = pathCollections.value.filter(
      (val) => val.collection_path_id !== pathCollection.value.collection_path_id
  )
  try {
    const response = axios.delete(
        'http://localhost:8080/collections/path/delete',
        {
          data:{
              collection_path_id: pathCollection.value.collection_path_id
            },
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    console.log(response)
  } catch (error) {
    console.error(error)
  }
  deletePathDialog.value = false
  pathCollection.value = {}
  toast.add({severity: 'success', summary: '成功', detail: '路线已被删除!', life: 3000})
}

//===

onMounted(async () => {
  try {
    const response1 = await axios.get(
        `http://localhost:8080/collections/poi/listAll?account=${encodeURIComponent(account)}`,
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    products.value = response1.data
  } catch (error) {
    console.error(error)
  }
  //
  try {
    const response2 = await axios.get(
        `http://localhost:8080/collections/path/listAll?account=${encodeURIComponent(account)}`,
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    pathCollections.value = response2.data
  } catch (error) {
    console.error(error)
  }

  try {
    const response3 = await axios.get(
        `http://localhost:8080/user/address?account=${encodeURIComponent(account)}`,
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: getToken()
          }
        }
    )
    console.log('常用地点加载完成')
    console.log(response3.data)
    commonAddress.value = response3.data.address
  } catch (error) {
    console.error(error)
  }
})
</script>

<template>
  <main>
    <div style="width: 100%; height: 100%">
      <div>
        <div class="card">
          <Toolbar class="mb-4">
            <template #start>
              <div style="display: flex; justify-content: center">
                <SelectButton v-model="value" :options="options" aria-labelledby="basic"/>
              </div>
            </template>

            <template #end>
              <FileUpload
                  mode="basic"
                  accept="image/*"
                  :maxFileSize="1000000"
                  label="Import"
                  chooseLabel="导入"
                  class="mr-2 inline-block"
              />
              <Button label="下载" icon="pi pi-upload" severity="help" @click="exportCSV($event)"/>
            </template>
          </Toolbar>

          <DataTable
              ref="dt"
              :value="products"
              v-model:selection="selectedProducts"
              dataKey="id"
              :paginator="true"
              :rows="10"
              :filters="filters"
              paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
              :rowsPerPageOptions="[5, 10, 25]"
              currentPageReportTemplate="展示 {first} 到 {last} 的 {totalRecords} 地点"
              v-if="value === '地点'"
          >
            <template #header>
              <div class="flex flex-wrap gap-2 align-items-center justify-content-between">
                <h4 class="m-0">地点</h4>

                <IconField iconPosition="left">
                  <InputIcon>
                    <i class="pi pi-search"/>
                  </InputIcon>
                  <InputText v-model="filters['global'].value" placeholder="搜索..."/>
                </IconField>
              </div>
            </template>
            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <Column
                field="collection_location_id"
                header="林荫道序号"
                sortable
                style="min-width: 12rem"
            ></Column>
            <Column
                field="collection_location"
                header="地点"
                sortable
                style="min-width: 16rem"
            ></Column>

            <Column
                field="collection_location_info"
                header="描述"
                sortable
                style="min-width: 16rem"
            ></Column>
            <Column :exportable="false" style="min-width: 8rem">
              <template #body="slotProps">
                <Button
                    icon="pi pi-trash"
                    outlined
                    rounded
                    severity="danger"
                    @click="confirmDeleteProduct(slotProps.data)"
                />
              </template>
            </Column>
          </DataTable>

          <Dialog
              v-model:visible="deleteProductDialog"
              :style="{ width: '450px' }"
              header="Confirm"
              :modal="true"
          >
            <div class="confirmation-content">
              <i class="pi pi-exclamation-triangle mr-3" style="font-size: 2rem"/>
              <span v-if="product"
              >您想要删除 <b>{{ product.collection_location }}</b
              >?</span
              >
            </div>
            <template #footer>
              <Button label="取消" icon="pi pi-times" text @click="deleteProductDialog = false"/>
              <Button label="删除" icon="pi pi-check" text @click="deleteProduct"/>
            </template>
          </Dialog>
          <DataTable
              ref="dt"
              :value="pathCollections"
              v-model:selection="selectedPaths"
              dataKey="id"
              :paginator="true"
              :rows="10"
              :filters="filters"
              paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
              :rowsPerPageOptions="[5, 10, 25]"
              currentPageReportTemplate="显示 {first} 到 {last} 的 {totalRecords} 个路线"
              v-if="value === '路线'"
          >
            <template #header>
              <div class="flex flex-wrap gap-2 align-items-center justify-content-between">
                <h4 class="m-0">路线</h4>

                <IconField iconPosition="left">
                  <InputIcon>
                    <i class="pi pi-search"/>
                  </InputIcon>
                  <InputText v-model="filters['global'].value" placeholder="搜索..."/>
                </IconField>
              </div>
            </template>
            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <Column
                field="collection_path_origin"
                header="起点"
                sortable
                style="min-width: 16rem"
            ></Column>

            <Column
                field="collection_path_destination"
                header="终点"
                sortable
                style="min-width: 16rem"
            ></Column>
            <Column field="distance" header="距离" sortable style="min-width: 12rem"></Column>
            <Column
                field="tree_line"
                header="林荫道占比"
                sortable
                style="min-width: 12rem"
            ></Column>
            <Column field="time" header="用时" sortable style="min-width: 12rem"></Column>
            <Column field="mark" header="评分" sortable style="min-width: 12rem">
              <template #body="slotProps">
                <Rating :modelValue="slotProps.data.mark" :readonly="true" :cancel="false"/>
              </template>
            </Column>
            <Column :exportable="false" style="min-width: 8rem">
              <template #body="slotProps">
                <Button
                    icon="pi pi-trash"
                    outlined
                    rounded
                    severity="danger"
                    @click="confirmDeletePath(slotProps.data)"
                />
              </template>
            </Column>
          </DataTable>
          <Dialog
              v-model:visible="deletePathDialog"
              :style="{ width: '450px' }"
              header="Confirm"
              :modal="true"
          >
            <div class="confirmation-content">
              <i class="pi pi-exclamation-triangle mr-3" style="font-size: 2rem"/>
              <span v-if="pathCollection"
              >你想要删除 <b>{{ pathCollection.collection_location }}</b
              >?</span
              >
            </div>
            <template #footer>
              <Button label="取消" icon="pi pi-times" text @click="deletePathDialog = false"/>
              <Button label="删除" icon="pi pi-check" text @click="deletePath"/>
            </template>
          </Dialog>

          <!--  -->
          <DataTable
              ref="dt"
              :value="commonAddress"
              v-model:selection="selectedCommonAddress"
              dataKey="id"
              :paginator="true"
              :rows="10"
              :filters="filters"
              paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
              :rowsPerPageOptions="[5, 10, 25]"
              currentPageReportTemplate="从 {first} 到 {last} 的 {totalRecords} 个地点"
              v-if="value === '常用地址'"
          >
            <template #header>
              <div class="flex flex-wrap gap-2 align-items-center justify-content-between">
                <h4 class="m-0">常用地址</h4>

                <IconField iconPosition="left">
                  <InputIcon>
                    <i class="pi pi-search"/>
                  </InputIcon>
                  <InputText v-model="filters['global'].value" placeholder="搜索..."/>
                </IconField>
              </div>
            </template>
            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <Column
                field="user_address_id"
                header="地址序号"
                sortable
                style="min-width: 12rem"
            ></Column>
            <Column
                field="user_address"
                header="常用地点"
                sortable
                style="min-width: 16rem"
            ></Column>

            <Column field="user_comment" header="描述" sortable style="min-width: 16rem"></Column>
            <Column :exportable="false" style="min-width: 8rem">
              <template #body="slotProps">
                <Button
                    icon="pi pi-pencil"
                    outlined
                    rounded
                    class="mr-2"
                    @click="editProduct(slotProps.data)"
                />
                <Button
                    icon="pi pi-trash"
                    outlined
                    rounded
                    severity="danger"
                    @click="confirmDeleteAddress(slotProps.data)"
                />
              </template>
            </Column>
          </DataTable>
          <Dialog
              v-model:visible="AddressDialog"
              :style="{ width: '450px' }"
              header="常用地点"
              :modal="true"
              class="p-fluid"
          >
            <div class="field">
              <label for="name">地点</label>
              <InputText
                  id="name"
                  v-model.trim="address.user_address"
                  required="true"
                  autofocus
                  :invalid="submitted && !product.name"
              />
              <small class="p-error" v-if="submitted && !product.name">Name is required.</small>
            </div>
            <div class="field">
              <label for="description">描述</label>
              <Textarea
                  id="description"
                  v-model="address.user_comment"
                  required="true"
                  rows="3"
                  cols="20"
              />
            </div>
            <template #footer>
              <Button label="取消" icon="pi pi-times" text @click="hideDialog"/>
              <Button label="保存" icon="pi pi-check" text @click="saveProduct"/>
            </template>
          </Dialog>

          <Dialog
              v-model:visible="deleteAddressDialog"
              :style="{ width: '450px' }"
              header="Confirm"
              :modal="true"
          >
            <div class="confirmation-content">
              <i class="pi pi-exclamation-triangle mr-3" style="font-size: 2rem"/>
              <span v-if="product"
              >您想要删除 <b>{{ product.collection_location }}</b
              >?</span
              >
            </div>
            <template #footer>
              <Button label="取消" icon="pi pi-times" text @click="deleteAddressDialog = false"/>
              <Button label="删除" icon="pi pi-check" text @click="deleteAddress"/>
            </template>
          </Dialog>
          <!--  -->
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped></style>
