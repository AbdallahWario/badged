add_library(lash SHARED IMPORTED)
set_target_properties(lash PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/libqaeda/src/aux/liblash/src/liblash.so)

add_library(
        qaeda
        SHARED
        org_defalsified_android_badged_services_LibQaeda.c
        libqaeda/src/store/dummy.c
)

target_include_directories(qaeda PRIVATE
        libqaeda/src
        libqaeda/src/aux/include
)

target_link_libraries(qaeda
        android
        log
        lash
)