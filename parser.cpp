#include <iostream>
#include <vector>
#include <stdlib.h>
#include "parser.hpp"

// Parses the lines and creates a corresponding matrix of Fields.
std::vector<std::vector<Field>> Parser::parse(std::vector<std::string> lines) {
    std::cout << std::endl << "Parsing input:" << std::endl;
    for (auto line : lines) {
        std::cout << line << std::endl;
    }

    // Determine number of rows and columns
    auto rows = lines.size();
    if (rows == 0) {
        std::cerr << "No rows in screen!" << std::endl;
        abort();
    }
    auto cols = max_line_length(lines);
    if (cols == 0) {
        std::cerr << "No columns in screen!" << std::endl;
        abort();
    }

    return parse_screen(lines, rows, cols);
}

// Returns the length of the longest string in given vector of strings.
size_t Parser::max_line_length(const std::vector<std::string> &lines) {
    size_t max = 0;
    for (const auto &line : lines) {
        if (line.length() > max) {
            max = line.length();
        }
    }
    return max;
}

// Parses the lines and creates a corresponding <rows> x <cols> matrix of Fields. Removes the outmost columns and rows, since they are walls.
std::vector<std::vector<Field>> Parser::parse_screen(const std::vector<std::string> lines, const size_t rows, const size_t cols) {

    std::vector<std::vector<Field>> screen = create_matrix(rows-2, cols-2, Field::EMPTY);

    for (size_t i = 0; i < rows-2; i++) {
        std::string s = lines[i+1];
        std::vector<Field> &row = screen[i];

        for (size_t j = 0; j < cols-2 && j < s.length(); j++) {
            row[j] = field_functions::convertFromScreenInput(s[j+1]);
        }
    }
    return screen;
}

// create a 'matrix' (vector of vectors) of size <rows> x <cols>, filled with <initial_value>
std::vector<std::vector<Field>> Parser::create_matrix(const size_t rows, const size_t cols, const Field initial_value) {
    std::vector<std::vector<Field>> result;
    for (size_t i = 0; i < rows; i++) {
        std::vector<Field> row;
        row.assign(cols, initial_value);
        result.push_back(row);
    }
    return result;
}
