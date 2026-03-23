SRC_DIR = src
LIB_DIR = lib
BIN_DIR = bin
MAIN = MainRescate

CP = $(LIB_DIR)/*:$(BIN_DIR)

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

all: compile

compile:
	mkdir -p $(BIN_DIR)
	javac -encoding UTF-8 -cp "$(LIB_DIR)/*:$(SRC_DIR)" -d $(BIN_DIR) $(SOURCES)

run: compile
	java -cp "$(CP)" $(MAIN)

clean:
	rm -rf $(BIN_DIR)

