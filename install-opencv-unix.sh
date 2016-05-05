#!/bin/bash

OPENCV_VERSION=3.1.0

HELPFUL_GUIDES="
http://www.learnopencv.com/install-opencv-3-on-yosemite-osx-10-10-x/
http://rodrigoberriel.com/2014/10/installing-opencv-3-0-0-on-ubuntu-14-04/
"


echo "Installing opencv-$OPENCV_VERSION for Mac/Linux ...\n"
echo "================================================================================
Make sure that you have the required dependencies!
Linux:
    sudo apt-get -y install libopencv-dev build-essential cmake git
    libgtk2.0-dev pkg-config python-dev python-numpy libdc1394-22
    libdc1394-22-dev libjpeg-dev libpng12-dev libtiff4-dev libjasper-dev
    libavcodec-dev libavformat-dev libswscale-dev libxine-dev
    libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev libv4l-dev libtbb-dev
    libqt4-dev libfaac-dev libmp3lame-dev libopencore-amrnb-dev
    libopencore-amrwb-dev libtheora-dev libvorbis-dev libxvidcore-dev x264
    v4l-utils unzip

Mac:
    Install Xcode Command Line Tools: http://railsapps.github.io/xcode-command-line-tools.html
    Install Homebrew: http://brew.sh
    Install cmake: brew install cmake pkg-config
    Install image libraries: brew install jpeg libpng libtiff openexr
    Install optimization libraries: brew install eigen tbb
================================================================================
"
echo "Continue install? [y|n]"
read ans
if [[ ! $ans =~ ^[Yy]$ ]]; then
    exit
fi

cd lib
echo "$Downloading opencv-$OPENCV_VERSION"
wget -c https://github.com/Itseez/opencv/archive/$OPENCV_VERSION.zip
unzip $OPENCV_VERSION.zip
rm -f $OPENCV_VERSION.zip

echo "Building opencv-$OPENCV_VERSION"
cd opencv-$OPENCV_VERSION
mkdir -p build
cd build
cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_TBB=ON -D WITH_V4L=OFF -D WITH_OPENGL=OFF ..
make -j4

if [[ $? = 0 ]]; then
    echo "Finished building opencv-$OPENCV_VERSION"
    echo "Import opencv.userlibraries to your Eclipse user libraries to use Java bindings for opencv"
else
    echo "There were errors building opencv-$OPENCV_VERSION :("
    echo "Try googling the errors produced above for solutions."
fi
