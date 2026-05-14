cmake_minimum_required(VERSION 3.22.1)
project("termidroid")

# Compile term-bridge.cpp into a shared library called libtermidroid.so
add_library(termidroid SHARED term-bridge.cpp)

# Link the standard Android logging library
find_library(log-lib log)
target_link_libraries(termidroid ${log-lib})

