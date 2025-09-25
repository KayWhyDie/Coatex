# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Define the project to avoid CMake developer warnings.
project(KriptexApp)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        app

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ./kill_tor.cpp)

target_include_directories(app PRIVATE ${ANDROID_NDK}/sysroot/usr/include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        app

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

# Ensure shared libraries are linked with a 16KB page size to be compatible
# with Android 14+ page-size alignment checks. This app packages small native
# libs and may run on devices/emulators where the kernel uses 16KB pages.
# Add the linker option so the produced ELF uses a compatible max page size.
if(NOT DEFINED CMAKE_SHARED_LINKER_FLAGS)
        set(CMAKE_SHARED_LINKER_FLAGS "")
endif()
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
