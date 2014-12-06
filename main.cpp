#include <iostream>
#include <fstream>
#include "parser.hpp"
#include "smv_writer.hpp"
#include "nusmv_result_handler.hpp"
#include <sstream>
#include <iomanip>
#include <string.h>
#include <chrono>

std::vector<std::string> read_lines(char *file_path);

void runNuSmv(char *file_path);

void handleNuSMVResult(const std::string &output);

int main(int argc, char *argv[]) {
    if (argc < 2) {
        std::cerr << "Usage: sokoban-nusmv-generator <input_file>" << std::endl;
        return -1;
    }

    char smvFile[128];
    strcpy(smvFile, argv[1]);
    strcat(smvFile, ".smv");

    Parser parser;
    SmvWriter smvWriter;

    std::vector<std::string> lines = read_lines(argv[1]);
    std::vector<std::vector<Field>> screen = parser.parse(lines);
    smvWriter.writeSmv(screen, smvFile);

    char runNuSMV;
    while (runNuSMV != 'y' && runNuSMV != 'n') {
        std::cout << "Run NuSMV on generated file? (y/n) > ";
        std::cin >> runNuSMV;
    }

    if (runNuSMV == 'y') {
        runNuSmv(smvFile);
    }

    return 0;
}

// Read file into vector of strings
std::vector<std::string> read_lines(char *file_path) {
    std::vector<std::string> result;

    std::ifstream ifs;
    ifs.open(file_path, std::ifstream::in);
    for (std::string line; std::getline(ifs, line);) {
        // get rid of trailing whitespace
        line = line.substr(0, line.find_last_not_of(" \t") + 1);
        if (line.length() > 0) result.push_back(line);
    }
    ifs.close();

    return result;
}

std::string exec(char *cmd) {
    FILE *pipe = popen(cmd, "r");
    if (!pipe) return "ERROR";
    char buffer[128];
    std::string result = "";
    while (!feof(pipe)) {
        if (fgets(buffer, 128, pipe) != NULL)
            result += buffer;
    }
    pclose(pipe);
    return result;
}

void runNuSmv(char *file_path) {
    auto start = std::chrono::system_clock::now();

    std::cout << "Running NuSMV..." << std::endl;
    std::string cmd = std::string("NuSMV ") + file_path;
    char *cstr = new char[cmd.length() + 1];
    strcpy(cstr, cmd.c_str());
    std::string result = exec(cstr);
    handleNuSMVResult(result);
    delete[] cstr;

    auto now = std::chrono::system_clock::now();
    auto dur = now - start;
    typedef std::chrono::duration<float> float_seconds;
    auto secs = std::chrono::duration_cast<float_seconds>(dur);

    std::cout << "Done. Took " << secs.count() << " seconds" << std::endl;
}

void handleNuSMVResult(const std::string &result) {
    NuSmvResultHandler nuSmvResultHandler;
    nuSmvResultHandler.handle(result);
}
