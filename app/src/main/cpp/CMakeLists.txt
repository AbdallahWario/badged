#cmake_minimum_required(VERSION 3.10)
#project(badged_app)
#
##  library directory
#set(LIB_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI})
#set(INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libs/include)
#
## Import pre-built libqaeda static library (includes all dependencies)
#add_library(qaeda-static STATIC IMPORTED)
#set_target_properties(qaeda-static PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libqaeda.a)
#
## Include directories
##include_directories(
##        ${CMAKE_CURRENT_SOURCE_DIR}
##        ${INCLUDE_DIR}
#)
#
## JNI wrapper library that uses the imported static library
#add_library(qaeda SHARED
#        # JNI bridge files
##        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_LibQaeda.c
##        ${CMAKE_CURRENT_SOURCE_DIR}/org_defalsified_android_badged_services_Cert.c
#)
#
## Link against qaeda library and needed system libraries
#target_link_libraries(qaeda
#        qaeda-static
#        android
#        log
#        atomic
#        m
#)