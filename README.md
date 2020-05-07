
# Builder's Bag [![](http://cf.way2muchnoise.eu/buildersbag.svg)](https://minecraft.curseforge.com/projects/buildersbag) [![](http://cf.way2muchnoise.eu/versions/buildersbag.svg)](https://minecraft.curseforge.com/projects/buildersbag)

To use Builder's Bag in your projects, include this in your build.gradle:
```
repositories {
	maven {
		url "https://maven.blamejared.com/"
	}
}

dependencies {
	deobfCompile "tschipp.buildersbag:buildersbag-MCVERSION:MODVERSION" 
}
```
Make sure to replace `MCVERSION` and `MODVERSION` with the appropriate versions.
