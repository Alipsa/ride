library("grDevices")
library("graphics")
library("poker")
library("adwordsR")
print("libraries loaded")

pkgs <- installed.packages()
#str(pkgs)
#print(pkgs)

#print(library()) # different from GNU R
#print((.packages()))
pkgs <- (.packages())
#str(pkgs)
print(pkgs[2])