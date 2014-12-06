#ifndef SMV_WRITER_H
#define SMV_WRITER_H

#include "field.hpp"

class SmvWriter {

public:
    void writeSmv(const std::vector<std::vector<Field>> &screen, const char *file_path);

private:
    void writeSquareModule(std::ofstream &output);

    void writeMainModule(const std::vector<std::vector<Field>> &screen, std::ofstream &output);

    void writeSquareInstances(const std::vector<std::vector<Field>> &screen, std::ofstream &output);

    void writeSpec(const std::vector<std::vector<Field>> &screen, std::ofstream &output);
};

#endif // SMV_WRITER_H