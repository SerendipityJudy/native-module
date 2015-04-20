#**概述**

简介：
一款文本阅读器，自带书架、翻页、进度设置功能。

设置：
点击安卓菜单键可以弹出设置菜单
阅读界面采用羊皮纸风格，翻页采用模拟iReader的仿真翻页方式。
在添加图书菜单内可以对手机图书进行移动、重命名等操作。

测试用例：
初次启动书架需要点击安卓菜单键添加一本txt图书。
使用测试用例时需要首先添加fileBrowser模块。

#**registerApp**<div id="a1"></div>

打开阅读器

openBook({params})

##params

path：

- 类型：字符串
- 默认值：空
- 描述：打开图书路径


##示例代码

```js
var iBookReader = null;

apiready = function(){
	iBookReader = api.require('iBookReader');
}

function openBook(){
	var param = {path:'你的图书路径'};
	iBookReader.openBook(param);
}
```

##可用性

Android系统

可提供的1.1.0及更高版本

#**registerApp**<div id="a1"></div>

打开阅读器书架

openBookShelf()

##示例代码

```js
var iBookReader = null;

apiready = function(){
	iBookReader = api.require('iBookReader');
}

function openBookShelf(){
	iBookReader.openBookShelf();
}
```

##可用性

Android系统

可提供的1.0.0及更高版本