# What is this?

This is a image compress/scale tool based on java ImageIO and Graphics2D.

It supports JPEG file, PNG file and WEBP file on Windows/Linux/Mac

There are two Compressor impl in this library:

1. The first is `CustomCompressor`, can compress more but the image quality is worse.
2. The other is `ThumbnailCompressor`, based on Google thumbnails library.

There are also two ways for you to use this tool:

1. You can use it as a command line tool to compress local images
2. Or you can also use it as an external library in you JVM project

## Usage way 1: runnable jar

First you need compile a runnable jar or download one at the release tab of github,
and then run it.

```bash
# run below line and then you can get the runnable jar at ./build/libs/ImageUtil-all.jar
./gradlew shadowJar

# move it to your desktop
mv ./build/libs/ImageUtil-all.jar ~/Desktop/image.jar

# run it by empty args to get manual
java -jar image.jar
```

## Usage way 2: third-part jar

First you need config you build.gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.chavin-chen:util-image:1.0.1'
}
```

And then you use it in your project:

```java
public class Main {
    public static void main(String[] args) {
        ICompressor compressor = createCompressor(false);
        var srcFile = new File("xxx.png");
        var dstFile = new File("xxx-opt.png");
        compressor.compress(srcFile, dstFile, 0.75);
    }

    private static ICompressor createCompressor(boolean useThumbCompressor) {
        if (useThumbCompressor) {
            return new ThumbnailCompressor();
        }
        return new CustomCompressor();
    }
}

```


**But to be honest**, if you need more compression ratio and better image quality,
I suggest you use [TinyPNG](https://tinypng.com/) or [Squoosh](https://squoosh.app/)

## LICENSE

```plain

Copyright (c) 2019-present, ImageUtil Contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```
