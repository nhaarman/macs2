cmake_minimum_required(VERSION 2.8.4)
project(sokoban_nusmv_generator)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

set(SOURCE_FILES
    main.cpp
    field.hpp field.cpp
    parser.hpp parser.cpp
    smv_writer.hpp smv_writer.cpp
    nusmv_result_handler.hpp nusmv_result_handler.cpp
)
add_executable(sokoban_nusmv_generator ${SOURCE_FILES})