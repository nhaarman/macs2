#include <iostream>
#include <stdlib.h>
#include "field.hpp"

namespace field_functions {

    Field convertFromScreenInput(const char c) {
        switch (c) {
            case '#':
                return Field::WALL;
            case '@':
                return Field::MAN;
            case '.':
                return Field::GOAL;
            case ' ':
                return Field::EMPTY;
            case '$':
                return Field::BLOCK;
            case '*':
                return Field::BLOCK_ON_GOAL;
            case '+':
                return Field::MAN_ON_GOAL;
            default:
                std::cerr << "Invalid character '" << c << "' in screen!" << std::endl;
                abort();
        }
    }

    std::string convertToSmvString(const Field field) {
        switch (field) {
            case Field::WALL:
                return "w";
            case Field::MAN:
                return "m";
            case Field::GOAL:
                return "g";
            case Field::EMPTY:
                return "e";
            case Field::BLOCK:
                return "b";
            case Field::BLOCK_ON_GOAL:
                return "bog";
            case Field::MAN_ON_GOAL:
                return "mog";
        }
    }
}