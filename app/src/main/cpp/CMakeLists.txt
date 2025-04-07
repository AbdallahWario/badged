cmake_minimum_required(VERSION 3.10)
project(badged_app)

set(LIBQAEDA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libqaeda)
set(LIBQAEDA_SRC_DIR ${LIBQAEDA_DIR}/src)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${LIBQAEDA_SRC_DIR}
        ${LIBQAEDA_SRC_DIR}/aux/include
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/hex
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/rerr
)

add_library(qaeda SHARED
        # JNI Interface
        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_LibQaeda.c

        ${LIBQAEDA_SRC_DIR}/store/dummy.c

        # Required hex functionality from liblash
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/hex/hex.c
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/rerr/rerr.c
)

target_compile_definitions(qaeda PRIVATE
        ANDROID=1
)

# Linking against Android libraries
target_link_libraries(qaeda
        android
        log
        atomic
        m
)

# Create main app library
add_library(badged SHARED
        native-lib.cpp
)

# Link app against our JNI wrapper
target_link_libraries(badged
        qaeda
        android
        log
)