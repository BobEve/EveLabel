# EveLabel
图片添加贴纸

## Summary 
```java
// 设置原图
eveLabelView.setBackgroundBitmap(Bitmap bg)

// 设置贴纸
eveLabelView.addLabel(Bitmap bg)

//  获取合成图片
Bitmap result = eveLabelView.createResultBitmap()

// 获取合成图片存储与file中，注意处理抛出异常
eveLabelView.createResultBitmapWithFile(String file)

```
so easy!
以上4步即可基本完成图片贴纸，你需要做的就是数据源处理了。
欢迎大家提出意见！

##TODO
1. LabelView宽高比例设置
2. 图片滤镜
3. 文字Tag标签
4. 是否封装完整的取图、切图、贴纸、标签、滤镜流程

# Licensed
<br />Copyright 2017 BobEve.<br />
<br />Licensed under the Apache License, Version 2.0 (the "License");
<br />you may not use this file except in compliance with the License.
<br />You may obtain a copy of the License at
<br />
<i>http://www.apache.org/licenses/LICENSE-2.0</i>
<br />Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions
and limitations under the License.<br />
