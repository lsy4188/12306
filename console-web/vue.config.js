module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://192.168.170.1:8081',
        changeOrigin: true,
        ws: true
      }
    },
    client: {
      overlay: false
    }
  },
  css: {
    loaderOptions: {
      less: {
        javascriptEnabled: true,
        modifyVars: {
          'border-radius-base': '4px',
          'card-radius': '4px'
        }
      }
    }
  }
}
