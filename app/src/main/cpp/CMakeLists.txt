cmake_minimum_required(VERSION 3.10)
project(badged_app)

#  library directory
set(LIB_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI})
set(INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libs/include)

#  pre-built libraries
add_library(tasn1 STATIC IMPORTED)
set_target_properties(tasn1 PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libtasn1.so)

add_library(gcrypt STATIC IMPORTED)
set_target_properties(gcrypt PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libgcrypt.so)

add_library(gpg-error STATIC IMPORTED)
set_target_properties(gpg-error PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libgpg-error.so)

add_library(qaeda-static STATIC IMPORTED)
set_target_properties(qaeda-static PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libqaeda.a)

# Include directories
include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${INCLUDE_DIR}
)



# JNI wrapper
add_library(qaeda SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_Cert.c
)

# Link against all libraries
target_link_libraries(qaeda
        qaeda-static
        tasn1
        gcrypt
        gpg-error
        android
        log
        atomic
        m
)