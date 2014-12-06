#include <iostream>
#include <fstream>
#include "parser.hpp"
#include "nusmv_result_handler.hpp"
#include <sstream>
#include <iomanip>

void NuSmvResultHandler::handle(std::string const result) {

    std::stringstream ss(result);
    std::string firstLine;

    std::getline(ss, firstLine, '\n');
    if (firstLine.substr(0, 3) == "***") {

        std::string line = "";
        while (line.substr(0, 2) != "--") {
            std::getline(ss, line, '\n');
        }

        std::cout << line << std::endl;


        std::string lastInput;
        while (std::getline(ss, line, '\n')) {
            if (line.substr(0, 8) == "-> Input") {
                std::getline(ss, line, '\n');
                if (line.substr(0, 2) != "->") {
                    lastInput = line;
                }
                std::cout << lastInput << std::endl;
            }
        }

        std::cout << line << std::endl;
    }
}