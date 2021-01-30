# 地图选取插件）

提供位置选取，位置预览能力

仿微信交互

<img src="D:\WorkSpace\xiaoyuan_project\cordova_project\plugin\cordova-plugin-location\image\Video_20210130_120747_208.gif" style="zoom: 66%;" /><img src="D:\WorkSpace\xiaoyuan_project\cordova_project\plugin\cordova-plugin-location\image\Screenshot_20210116_173239_io.cordova.hellocordova - 副本.jpg" style="zoom:25%;" /><img src="D:\WorkSpace\xiaoyuan_project\cordova_project\plugin\cordova-plugin-location\image\Screenshot_20210116_173208_io.cordova.hellocordova.jpg" style="zoom:25%;" />

### 坐标系

```
GCJ02
```

## 使用

### 调用

#### 1、1打开位置

```js
    cordova.exec(
        function success(){},
        function error(error){},
        'Location',
        'openLocation',[
            {
                "longitude":"116.452928",
                "latitude":"39.916963",
                "label":"呼家楼街道",
                "content":"中国北京市朝阳区呼家楼街道金汇路10号"
            }
        ])
```

#### 1、2参数说明

参数为 json 格式；

```json
{
     "longitude":"116.452928",
     "latitude":"39.916963",
     "label":"呼家楼街道",
     "content":"中国北京市朝阳区呼家楼街道金汇路10号"
}
```

| longitude | 经度     | 必选 |
| --------- | -------- | ---- |
| latitude  | 纬度     | 必选 |
| label     | 描述标题 | 可选 |
| content   | 描述内容 | 可选 |

#### 2、1选取定位位置

```js
  cordova.exec(function success(success){
        document.getElementById('location').innerHTML = JSON.stringify(success) ;
    },
    function error(error){

    }, 'Location', 'getLocation');
```

#### 2、2、返回值说明

返回值为 json 格式；

```json
{
    "longitude":"116.452928",
    "latitude":"39.916963",
    "label":"呼家楼街道",
    "address":"中国北京市朝阳区呼家楼街道金汇路10号",
    "CoordType":"GCJ02"
}
```

| longitude | 经度     |
| --------- | -------- |
| latitude  | 纬度     |
| label     | 位置标题 |
| address   | 位置描述 |
| CoordType | 坐标系   |

