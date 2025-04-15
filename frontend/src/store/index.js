import { createStore } from 'vuex';

const store = createStore({
  state() {
    return {
      accountName: '',  // 默认账户名称为空
    };
  },
  mutations: {
    setAccountName(state, name) {
      state.accountName = name;
    },
  },
  actions: {
    updateAccountName({ commit }, name) {
      commit('setAccountName', name);
    },
  },
});

export default store;
