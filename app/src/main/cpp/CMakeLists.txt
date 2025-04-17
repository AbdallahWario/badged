cmake_minimum_required(VERSION 3.10)
project(badged_app)

set(LIBQAEDA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libqaeda)
set(LIBQAEDA_SRC_DIR ${LIBQAEDA_DIR}/src)

# Adding libtasn1 static library
set(LIBTASN1_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI})
add_library(tasn1 STATIC IMPORTED)
set_target_properties(tasn1 PROPERTIES IMPORTED_LOCATION ${LIBTASN1_DIR}/libtasn1.a)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${LIBQAEDA_SRC_DIR}
        ${LIBQAEDA_SRC_DIR}/aux/include
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/hex
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/rerr
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/include  # libtasn1.h location
)

# Create qaeda library with all required sources
add_library(qaeda SHARED
        # JNI Interface files
        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_LibQaeda.c
        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_Cert.c

         # Core functionality from libqaeda
        ${LIBQAEDA_SRC_DIR}/store/dummy.c
        ${LIBQAEDA_SRC_DIR}/lq/cert.c
        ${LIBQAEDA_SRC_DIR}/lq/msg.c
        ${LIBQAEDA_SRC_DIR}/mem/std.c
        ${LIBQAEDA_SRC_DIR}/debug.c

        # Required hex functionality from liblash
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/hex/hex.c
        ${LIBQAEDA_SRC_DIR}/aux/liblash/src/rerr/rerr.c
)

# Add compile definitions
target_compile_definitions(qaeda PRIVATE
        ANDROID=1
)

# Linking against the libraries
target_link_libraries(qaeda
        tasn1
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