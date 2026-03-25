SRC_DIR = src
LIB_DIR = lib
BIN_DIR = bin
MAIN = MainRescate
EXP1 = Experimento1Operadores

# Separador del classpath
CPSEP_LINUX = :
CPSEP_WINDOWS = ;

# Classpath per executar Java
CP_RUN_LINUX = $(LIB_DIR)/*$(CPSEP_LINUX)$(BIN_DIR)
CP_RUN_WINDOWS = $(LIB_DIR)/*$(CPSEP_WINDOWS)$(BIN_DIR)

# Classpath per compilar (incloent el codi font)
CP_JAVAC_LINUX = $(LIB_DIR)/*$(CPSEP_LINUX)$(SRC_DIR)
CP_JAVAC_WINDOWS = $(LIB_DIR)/*$(CPSEP_WINDOWS)$(SRC_DIR)

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

all: compile

compile-linux:
	mkdir -p $(BIN_DIR)
	javac -encoding UTF-8 -cp "$(CP_JAVAC_LINUX)" -d $(BIN_DIR) $(SOURCES)

compile-w:
	mkdir -p $(BIN_DIR)
	javac -encoding UTF-8 -cp "$(CP_JAVAC_WINDOWS)" -d $(BIN_DIR) $(SOURCES)

compile: compile-linux

run: compile-linux
	java -cp "$(CP_RUN_LINUX)" $(MAIN)

run-w: compile-w
	java -cp "$(CP_RUN_WINDOWS)" $(MAIN)

exp1: compile-linux
	java -cp "$(CP_RUN_LINUX)" $(EXP1)

exp1-w: compile-w
	java -cp "$(CP_RUN_WINDOWS)" $(EXP1)

clean:
	rm -rf $(BIN_DIR)

