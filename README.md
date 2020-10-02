# Couchbase Mobile Field Demo
# Android app submodule


### Adding Couchbase Dependencies
There are two files that need to be modified in order to add the android dependencies:

- build.gradle (Project)

    Add the following to the `allprojects` section
    
    ```
    maven {
       url "https://mobile.maven.couchbase.com/maven2/dev/"
    }
    ```

- build.gradle (Module)
    
    Add the following implementation in the `dependencies` section.  You may want to use the latest version of the library which can be found in the [Maven Repository](https://mvnrepository.com/artifact/com.couchbase.lite/couchbase-lite-android)

    `implementation 'com.couchbase.lite:couchbase-lite-android-ee:2.8.0'`

