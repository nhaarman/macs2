cmake_minimum_required(VERSION 2.8.4)
project(macs)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(macs ${SOURCE_FILES})

target_link_libraries(macs sylvan)

find_package (Threads)
target_link_libraries(macs ${CMAKE_THREAD_LIBS_INIT})