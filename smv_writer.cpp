#include <iostream>
#include <vector>
#include <fstream>
#include <sstream>
#include "smv_writer.hpp"

std::string getNeighbour(int row, int column, int deltaRow, int deltaColumn, int noOfRows, int noOfColumns, std::string ending, std::string valueIfWrong) {
    int checkedRow = row + deltaRow;
    if (checkedRow < 0 || checkedRow >= noOfRows) {
        return valueIfWrong;
    }

    int checkedColumn = column + deltaColumn;
    if (checkedColumn < 0 || checkedColumn >= noOfColumns) {
        return valueIfWrong;
    }

    std::stringstream output;
    output << 's' << checkedRow << "_" << checkedColumn << ending;

    return output.str();
}

void SmvWriter::writeSmv(std::vector<std::vector<Field>> const &screen, const char *file_path) {
    std::cout << std::endl << "Writing smv code into " << file_path << "." << std::endl;

    std::ofstream output;
    output.open(file_path);

    writeSquareModule(output);
    writeMainModule(screen, output);

    output.close();
}

void SmvWriter::writeSquareModule(std::ofstream &output) {
    output << "MODULE square(initValue, left, top, right, bottom, dleft, dtop, dright, dbottom, cil, cit, cir, cib, dcil, dcit, dcir, dcib, direction) " << std::endl <<
            "" << std::endl <<
            "  VAR " << std::endl <<
            "    value    : {w, m, g, e, b, bog, mog}; " << std::endl <<
            "" << std::endl <<
            "  ASSIGN " << std::endl <<
            "    init(value) := initValue; " << std::endl <<
            "" << std::endl <<
            "    -- On the basis of move, assign values to variables below " << std::endl <<
            "    next(value) := " << std::endl <<
            "        case " << std::endl <<
            "            value = g & comingBox                   : bog; " << std::endl <<
            "            (value = g | value = bog) & comingMan   : mog; " << std::endl <<
            "            comingBox                               : b; " << std::endl <<
            "            comingMan                               : m; " << std::endl <<
            "            value = mog & isMovingSomewhere         : g; " << std::endl <<
            "            value = m & isMovingSomewhere           : e; " << std::endl <<
            "            TRUE                                    : value; " << std::endl <<
            "        esac; " << std::endl <<
            " " << std::endl <<
            "  DEFINE " << std::endl <<
            " " << std::endl <<
            "--  Where can we go? " << std::endl <<
            "    canGoLeft := " << std::endl <<
            "        left = e | left = g | (left = b & (dleft = e | dleft = g)) | (left = bog & (dleft = e | dleft = g)); " << std::endl <<
            "    canGoUp := " << std::endl <<
            "        top = e | top = g | (top = b & (dtop = e | dtop = g)) | (top = bog & (dtop = e | dtop = g)); " << std::endl <<
            "    canGoRight := " << std::endl <<
            "        right = e | right = g | (right = b & (dright = e | dright = g)) | (right = bog & (dright = e | dright = g)); " << std::endl <<
            "    canGoDown := " << std::endl <<
            "        bottom = e | bottom = g | (bottom = b & (dbottom = e | dbottom = g)) | (bottom = bog & (dbottom = e | dbottom = g)); " << std::endl <<
            " " << std::endl <<
            "-- Additional defines " << std::endl <<
            "    isAMan := value = m | value = mog; " << std::endl <<
            "    isMovingLeft := isAMan & direction=l & canGoLeft; " << std::endl <<
            "    isMovingUp := isAMan & direction=u & canGoUp; " << std::endl <<
            "    isMovingRight := isAMan & direction=r & canGoRight; " << std::endl <<
            "    isMovingDown := isAMan & direction=d & canGoDown; " << std::endl <<
            "    isMovingSomewhere := isMovingLeft | isMovingUp | isMovingRight | isMovingDown; " << std::endl <<
            " " << std::endl <<
            "-- Single carries " << std::endl <<
            "    col := isMovingLeft; " << std::endl <<
            "    cot := isMovingUp; " << std::endl <<
            "    cor := isMovingRight; " << std::endl <<
            "    cob := isMovingDown; " << std::endl <<
            " " << std::endl <<
            "-- Double carries " << std::endl <<
            "    dcol := isMovingLeft & (left = b | left = bog); " << std::endl <<
            "    dcot := isMovingUp & (top = b | top = bog); " << std::endl <<
            "    dcor := isMovingRight & (right = b | right = bog); " << std::endl <<
            "    dcob := isMovingDown & (bottom = b | bottom = bog); " << std::endl <<
            " " << std::endl <<
            "-- Carry in " << std::endl <<
            "    comingMan := cil | cit | cir | cib; " << std::endl <<
            "    comingBox := dcil | dcit | dcir | dcib; " << std::endl <<
            " " << std::endl;
}

void SmvWriter::writeMainModule(const std::vector<std::vector<Field>> &screen, std::ofstream &output) {
    output << ""
            "MODULE main " << std::endl <<
            "  IVAR " << std::endl <<
            "    move     : {l, u, r, d}; " << std::endl <<
            "  VAR " << std::endl;

    writeSquareInstances(screen, output);
    writeSpec(screen, output);
}

void SmvWriter::writeSquareInstances(std::vector<std::vector<Field>> const &screen, std::ofstream &output) {
    int rows = (int) screen.size();
    for (int i = 0; i < rows; i++) {
        int cols = (int) screen[i].size();
        for (int j = 0; j < cols; j++) {

            output << "\ts" << i << "_" << j << "\t: square( ";
            output << field_functions::convertToSmvString(screen[i][j]) << ", ";

            // value to the left
            output << getNeighbour(i, j, 0, -1, rows, cols, ".value", "w") << ", ";

            // value above
            output << getNeighbour(i, j, -1, 0, rows, cols, ".value", "w") << ", ";

            // value to the right
            output << getNeighbour(i, j, 0, 1, rows, cols, ".value", "w") << ", ";

            // value below
            output << getNeighbour(i, j, 1, 0, rows, cols, ".value", "w") << ", ";

            // value two squares to the left
            output << getNeighbour(i, j, 0, -2, rows, cols, ".value", "w") << ", ";

            // value two squares above
            output << getNeighbour(i, j, -2, 0, rows, cols, ".value", "w") << ", ";

            // value two squares to the right
            output << getNeighbour(i, j, 0, 2, rows, cols, ".value", "w") << ", ";

            // value two squares below
            output << getNeighbour(i, j, 2, 0, rows, cols, ".value", "w") << ", ";

            // carry to the left
            output << getNeighbour(i, j, 0, -1, rows, cols, ".cor", "FALSE") << ", ";

            // carry above
            output << getNeighbour(i, j, -1, 0, rows, cols, ".cob", "FALSE") << ", ";

            // carry to the right
            output << getNeighbour(i, j, 0, 1, rows, cols, ".col", "FALSE") << ", ";

            // carry below
            output << getNeighbour(i, j, 1, 0, rows, cols, ".cot", "FALSE") << ", ";

            // carry two squares to the left
            output << getNeighbour(i, j, 0, -2, rows, cols, ".dcor", "FALSE") << ", ";

            // carry two squares above
            output << getNeighbour(i, j, -2, 0, rows, cols, ".dcob", "FALSE") << ", ";

            // carry two squares to the right
            output << getNeighbour(i, j, 0, 2, rows, cols, ".dcol", "FALSE") << ", ";

            // carry two squares below
            output << getNeighbour(i, j, 2, 0, rows, cols, ".dcot", "FALSE") << ", ";

            output << "move );" << std::endl;
        }
        output << std::endl;
    }
}

void SmvWriter::writeSpec(const std::vector<std::vector<Field>> &screen, std::ofstream &output) {
    bool first = true;
    output << "\tSPEC AG ! (";

    for (int i = 0; i < screen.size(); i++) {
        for (int j = 0; j < screen[i].size(); j++) {
            Field const &field = screen[i][j];
            if (field == Field::GOAL || field == Field::BLOCK_ON_GOAL || field == Field::MAN_ON_GOAL) {
                if (!first) {
                    output << " & ";
                }
                output << "s" << i << "_" << j << ".value = bog";
                first = false;
            }
        }
    }

    output << ")" << std::endl;
}
