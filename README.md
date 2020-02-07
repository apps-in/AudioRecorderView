# AudioRecorderView

[![](https://jitpack.io/v/apps-in/AudioRecorderView.svg)](https://jitpack.io/#apps-in/AudioRecorderView)

Modified version of [AudioRecordView widget](https://github.com/Armen101/AudioRecordView)

## Usage

Add to your root build.gradle:
```Groovy
allprojects {
	repositories {
	...
	maven { url "https://jitpack.io" }
	}
}
```

Add the dependency:
```Groovy
dependencies {
	implementation 'com.github.apps-in:AudioRecorderView:Version'
}
```

Add widget to layout xml file:
```Groovy
<com.visualizer.amplitude.AudioRecordView
	android:id="@+id/recordView"
	app:chunkAlignTo="center"
	app:chunkColor="chunkColor"
	app:chunkMinHeight="4dp"
	app:chunkRoundedCorners="true"
	app:chunkSpace="4dp"
	app:chunkWidth="4dp"
	app:majorTickColor="majorTickColor"
	app:minorTickColor="minorTickColor"
	app:timestampColor="timestampColor"
	app:timestampSize="10sp"
	app:timestampTypeface="timestampTypeface" />
```

Every 100 ms update state of widget by feeding new amplitude value:
```Groovy
recordView.update(amplitude);
```

To reset widget call `recreate()` function:
```Groovy
recordView.recreate();
```

### License
```
MIT License

Copyright (c) 2020 Ihor Nepomniashchyi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


