#!/usr/bin/env Rscript
library(ggplot2)
library(grid)

args <- commandArgs(T)
pdf(file=args[2], width=10, height=5)
par(mar=c(2, 3, 0, 8),mgp=c(2.1,1,0), cex = 1.6, xpd=1)
cnt <- read.table(args[1], header=T, sep="\t", na.strings=c("-"))
cnt = cnt[cnt$continuous == 0, ]
#Average number of violated SLAs per scenario
violatedSLAs = cnt[, c("scenario", "violatedSLAs")]

print(aggregate(violatedSLAs, by=list(violatedSLAs$scenario), FUN=mean, na.rm=TRUE))

#Number of violations for maxOnlines:
cat("Instances with a violation of maxOnlines:\n")
sc1 = cnt[cnt$maxOnline > 0, c("scenario", "violatedSLAs")]
print(sc1)


#Filter out unviolated SLAs
cnt = cnt[cnt$violatedSLAs != 0,];
cnt$singleResourceCapacity = NULL

#Re-organize the tables to only keep one type of violation per row
spreads = cnt;
spreads$among = NULL
spreads$splitAmong = NULL
spreads$maxOnline = NULL
spreads$constraint = 1
colnames(spreads)[4] <- "violations"

amongs = cnt;
amongs$spread = NULL
amongs$splitAmong = NULL
amongs$maxOnline = NULL
amongs$constraint = 2
colnames(amongs)[4] <- "violations"

splitAmongs = cnt;
splitAmongs$spread = NULL
splitAmongs$maxOnline = NULL
splitAmongs$among = NULL
splitAmongs$constraint = 3
colnames(splitAmongs)[4] <- "violations"

maxOnlines = cnt;
maxOnlines$spread = NULL
maxOnlines$splitAmong = NULL
maxOnlines$among = NULL
maxOnlines$constraint = 4
colnames(maxOnlines)[4] <- "violations"

# Append the sub results
constraints <- rbind(spreads, amongs, splitAmongs, maxOnlines)

constraints$scenario <- factor(cnt$scenario, labels = c("Vertical\nElasticity", "Horizontal\nElasticity", "Server\nFailure", "Boot\nStorm"))
constraints$constraint <- factor(constraints$constraint, labels = c("spread   ","among   ","splitAmong   ", "maxOnlines"))
# Distribution of the violations
constraints$violations = constraints$violations / constraints$violatedSLAs * 100	
ggplot(aes(y = violations, x = scenario, fill = constraint), data = constraints) + geom_boxplot(notch=TRUE) + theme_bw(26) + xlab("") + theme(legend.key = element_blank(), legend.position = "top", legend.key.size = unit(1.4, "cm")) + scale_fill_manual(name="",values=grey(c(0.95, 0.8, 0.5, 0.2)))

foo <- dev.off();