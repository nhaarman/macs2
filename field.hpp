#ifndef FIELD_H
#define FIELD_H

enum class Field {
    EMPTY, WALL, MAN, BLOCK, GOAL, BLOCK_ON_GOAL, MAN_ON_GOAL
};

namespace field_functions {

    Field convertFromScreenInput(const char c);

    std::string convertToSmvString(const Field field);
}

#endif // FIELD_H