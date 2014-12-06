#ifndef PARSER_H
#define PARSER_H

#include <vector>
#include "field.hpp"

class Parser {

public:
    /**
    * Parses the lines and creates a corresponding matrix of Fields.
    *
    * @param lines The lines to parse.
    *
    * @return The parsed screen as a matrix of Fields.
    */
    std::vector<std::vector<Field>> parse(std::vector<std::string> lines);

private:

    /**
    * Returns the length of the longest string in given vector of strings.
    *
    * @param lines The strings.
    *
    * @return The maximum line length.
    */
    static size_t max_line_length(const std::vector<std::string> &lines);

    /**
    * Parses the lines and creates a corresponding <rows> x <cols> matrix of Fields.
    *
    * @param lines The lines to parse.
    * @param rows  The number of rows in the screen.
    * @param cols  The number of columns in the screen.
    *
    * @return The parsed screen as a matrix of Fields.
    */
    static std::vector<std::vector<Field>> parse_screen(const std::vector<std::string> lines, const size_t rows, const size_t cols);

    /**
    * Creates a matrix (vector of vectors) of size <rows> x <cols> of Fields, filled with <initial_value>.
    *
    * @param rows          The number of rows the matrix should have.
    * @param cols          The number of columns the matrix should have.
    * @param initial_value The initial Field value the entries should have.
    *
    * @return The filled matrix.
    */
    static std::vector<std::vector<Field>> create_matrix(const size_t rows, const size_t cols, const Field initial_value);
};

#endif  // PARSER_H