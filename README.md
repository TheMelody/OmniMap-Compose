OmniMap Compose 🗺
===============
<a href="https://github.com/TheMelody/OmniMap-Compose/blob/main/LICENSE"><img alt="LICENSE" src="https://img.shields.io/github/license/TheMelody/OmniMap-Compose"/></a>  <img alt="issues" src="https://img.shields.io/github/issues/TheMelody/OmniMap-Compose?color=important"/>  <img alt="forks" src="https://img.shields.io/github/forks/TheMelody/OmniMap-Compose?color=blueviolet"/>  <img alt="stars" src="https://img.shields.io/github/stars/TheMelody/OmniMap-Compose?color=success"/>  <a href="https://juejin.cn/user/8451824316670/posts"><img alt="稀土掘金" src="https://img.shields.io/badge/%E7%A8%80%E5%9C%9F%E6%8E%98%E9%87%91-301-green?labelColor=%231e80FF&color=black" ></a>  <a href="https://www.zhihu.com/people/fq_halifax"><img src="https://img.shields.io/badge/dynamic/json?color=282c34&amp;labelColor=0084ff&amp;label=%E7%9F%A5%E4%B9%8E%E5%85%B3%E6%B3%A8&amp;query=%24.data.totalSubs&amp;url=https%3A%2F%2Fapi.spencerwoo.com%2Fsubstats%2F%3Fsource%3Dzhihu%26queryKey%3Dfq_halifax&amp;longCache=true" alt="知乎"></a>



Compose一键集成5大地图平台神器:
- ![百度](https://via.placeholder.com/15/4e6ef2/4e6ef2.png) **`百度地图`**
- ![腾讯](https://via.placeholder.com/15/E69B19/E69B19.png) **`腾讯地图`**
- ![高德](https://via.placeholder.com/15/f03c15/f03c15.png) **`高德地图`**
- ![华为](https://via.placeholder.com/15/1589F0/1589F0.png) **`华为花瓣地图`**
- ![谷歌](https://via.placeholder.com/15/1589F0/1589F0.png) **`谷歌地图`**


## 注意
5大地图平台，目前只有华为的花瓣地图只支持Android 7.0+，其他平台支持Android 5.0+

**使用时需注意**：

1.高德地图比例尺控件需要和地图Logo一起作用显示，腾讯地图不需要一起显示，可分开显示
2.高德地图有显示底图标注开关，腾讯地图没有
3.高德地图和腾讯地图的地图模式不同，腾讯地图：没有导航图，而腾讯的夜景图现在叫：暗色地图
4.腾讯地图，室内图需要找商务协助办理
5.腾讯地图不能切换地图语言
6.腾讯地图无法隐藏地图Logo，现提供修改Logo位置和边距+Logo缩放的参数配置
7.腾讯地图的BitmapDescriptor需要在获取到MapContext之后才能用，否则会返回null



### TODO List

- [x] 高德地图
- [ ] 腾讯地图，完善中...
- [ ] 百度地图
- [ ] 谷歌地图
- [ ] 华为花瓣地图(Android 7.0-12)
