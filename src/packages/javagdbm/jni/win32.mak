###########################################################################
#
# win32 makefile -- javagdbm\jni
# A component of the Greenstone digital library software
# from the New Zealand Digital Library Project at the
# University of Waikato, New Zealand.
#
# Copyright (C) 1999  The New Zealand Digital Library Project
#
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
#
###########################################################################

JAVAGDBMHOME = ..
 
GSDLHOME= $(GSDL3HOME)\gs2build
GDBMHOME = $(GSDLHOME)\common-src\packages\gdbm\gdbm-1.8.3

AR = lib
CC = cl
CPPFLAGS = 

DEFS = -D__WIN32__ -DHAVE_STRING_H

INCLUDES = -I"$(GDBMHOME)" -I. \
           -I"$(JAVA_HOME)\include" -I"$(JAVA_HOME)\include\win32"           

COMPILE = $(CC) -c $(CPPFLAGS) $(DEFS) $(INCLUDES)

.SUFFIXES:
.SUFFIXES: .c .obj
.c.obj:
	$(COMPILE) $<

ANSI2KNR = 
o = .obj
 
HEADERS = \
    GdbmFile.h

SOURCES = \
    gdbmjava.c

OBJECTS = \
    gdbmjava$o   "$(GDBMHOME)\gdbm.lib" 
 
all : compile link

compile:
        $(COMPILE) gdbmjava.c

link:
        $(CC) -MD -LD $(OBJECTS) -Fegdbmjava.dll

install: 
        copy gdbmjava.dll "$(GSDL3HOME)\lib\jni"

clean:
	del *$o 
