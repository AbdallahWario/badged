cmake_minimum_required(VERSION 3.22.1)
project(badged)
set(LIBQAEDA_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libqaeda/src)
set(LIBQAEDA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libqaeda)
include_directories(${LIBQAEDA_DIR}/src ${CMAKE_CURRENT_SOURCE_DIR})
file(GLOB_RECURSE LIBQAEDA_SOURCES ${LIBQAEDA_DIR}/src/*.c)
add_library(qaeda SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_LibQaeda.c
        ${LIBQAEDA_SOURCES}
)
target_link_libraries(qaeda android log atomic m)
add_library(badged SHARED native-lib.cpp)
target_link_libraries(badged qaeda android log)