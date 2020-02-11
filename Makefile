# SRC = all .c files
# OBJ = the list of all .o files corresponding to the .c files
	# represented as the commands needed to make them
	# These commands are run when the .o files are needed
# TOBJ = the list of all *_test.o files corresponding to the .c files
	# see above
# PROG = just a string literal, "simplec"

JC = javac

BUILDDIR = bin
JDOCDIR = doc
SRCDIR = src

SRC = $(wildcard $(SRCDIR)/*.java)

OBJ = $(SRC:$(SRCDIR)/%.java=$(BUILDDIR)/%.class)
DOC = $(SRC:$(SRCDIR)/%.java=$(JDOCDIR)/%.html)

# ################################
# Default commands
# ################################

.PHONY: all clean

all: $(OBJ)
test: $(TEST)
doc: $(OBJ) $(DOC)  # note: $^ == theSourceFileAndPathCurrentlyTargeted
run-example: $(OBJ)
	@java -cp $(BUILDDIR) Main

# ################################
# Production commands
# ################################

# # when "make" is called, this runs
# # note: $^ == $(SRC)
# compileAll: $(OBJ)
# 	$(JC) -cp $(SRCDIR)/ -d $(BUILDDIR)/ $^

# the command used by OBJ to make .o files using .c files
$(BUILDDIR)/%.class: $(SRCDIR)/%.java
# 	@mkdir -p $(@D)
	$(JC) -cp $(SRCDIR)/ -d $(BUILDDIR)/ $^




# ################################
# Documentation commands
# ################################

$(JDOCDIR)/%.html: $(SRCDIR)/%.java
	@javadoc -cp $(BUILDDIR)/ -d $(JDOCDIR)/ $^ 

# ################################
# Clean
# ################################

# remove all files that are part of PROG, TEST, OBJ, or TOBJ
clean:
	rm -rf $(PROG) $(TEST) $(OBJ) $(TOBJ)
