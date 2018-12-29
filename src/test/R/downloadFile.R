library("hamcrest")
library("org.renjin.cran:downloader")

url <- "http://raw.githubusercontent.com/genomicsclass/dagdata/master/inst/extdata/femaleControlsPopulation.csv"
filename <- basename(url)
print(filename)
#download(url, destfile=filename, quiet = FALSE, mode = "w", cacheOK = TRUE)
#download.file(url, destfile = filename, method = "wget")
download.file(url, destfile = filename)
if(file.exists(filename)) {
	print("success!")
	file.remove(filename)
} else {
	print("failed")
}
