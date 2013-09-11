#!/usr/bin/env Rscript
library(ggplot2)
library(grid)
library(plyr)

args <- commandArgs(T)
pdf(file=args[2], width=10, height=5)
par(mar=c(2, 3, 0, 8),mgp=c(2.1,1,0), cex = 1.6, xpd=1)
cnt <- read.table(args[1], header=T, sep="\t", na.strings=c("-"))
cnt = cnt[cnt$continuous == 0, ]

#Distribution of the actions, we ignore allcate
cnt$bootVM = cnt$bootVM / (cnt$actions - cnt$allocate) * 100
cnt$migrateVM = cnt$migrateVM / (cnt$actions - cnt$allocate) * 100
cnt$bootNode = cnt$bootNode / (cnt$actions - cnt$allocate) * 100
cnt$shutdownNode = cnt$shutdownNode / (cnt$actions - cnt$allocate) * 100
cnt$allocate = cnt$allocate / (cnt$actions - cnt$allocate) * 100

#Average number of violated SLAs per scenario
violatedSLAs = cnt[, c("scenario", "violatedSLAs","bootVM","migrateVM","bootNode","shutdownNode")]
g = aggregate(violatedSLAs, by=list(violatedSLAs$scenario), FUN=mean, na.rm=TRUE)
g$scenario <- factor(g$scenario, labels= c("Vertical Elasticity", "Horizontal Elasticity", "Server Failure", "Boot Storm"))
g$Group.1 = NULL
print(g)

#Number of violations for maxOnlines:
cat("Instances with a violation of maxOnlines:\n")
sc1 <- cnt[cnt$maxOnline > 0, c("scenario", "violatedSLAs")]
#sc1$scenario <- factor(sc1$scenario, labels= c("Vertical Elasticity", "Horizontal Elasticity", "Server Failure", "Boot Storm"))
print(sc1)

#Distribution of the actions. We ignore allocate actions
#cause they cannot lead to violations

#Filter out unviolated SLAs
cnt = cnt[cnt$violatedSLAs != 0,];
cnt = cnt [, c("instance","scenario","spread", "among", "splitAmong","maxOnline","violatedSLAs")];

#Re-organize the tables to only keep one type of violation per row
spreads = cnt;
colnames(spreads)[3] <- "violations"
spreads$among = NULL
spreads$splitAmong = NULL
spreads$maxOnline = NULL
spreads$constraint = 1

amongs = cnt;
colnames(amongs)[4] <- "violations"
amongs$spread = NULL
amongs$splitAmong = NULL
amongs$maxOnline = NULL
amongs$constraint = 2


splitAmongs = cnt;
colnames(splitAmongs)[5] <- "violations"
splitAmongs$spread = NULL
splitAmongs$maxOnline = NULL
splitAmongs$among = NULL
splitAmongs$constraint = 3

maxOnlines = cnt;
colnames(maxOnlines)[6] <- "violations"
maxOnlines$spread = NULL
maxOnlines$splitAmong = NULL
maxOnlines$among = NULL
maxOnlines$constraint = 4


# Append the sub results
constraints <- rbind(spreads, amongs, splitAmongs, maxOnlines)
# 2. Scenario type: 1=ve, 2=he, 3=sf, 4=bs
constraints$scenario <- factor(constraints$scenario, labels= c("Vertical\nElasticity", "Horizontal\nElasticity", "Server\nFailure", "Boot\nStorm"))
#constraints$scenario[constraints$scenario == "bs"] = "Boot\nStorm"
#constraints$scenario[constraints$scenario == "he"] = "Horizontal\nElasticity"
#constraints$scenario[constraints$scenario == "ve"] = "Vertical\nElasticity"
#constraints$scenario[constraints$scenario == "sf"] = "Server\nFailure"
#print(constraints[constraints$instance == "100.json", ])
constraints$constraint <- factor(constraints$constraint, labels = c("spread   ","among   ","splitAmong   ", "maxOnline"))
#print(constraints[constraints$instance == "100.json", ])
# Distribution of the violations
constraints$violations = constraints$violations / constraints$violatedSLAs * 100	
ggplot(aes(y = violations, x = scenario, fill = constraint), data = constraints) + geom_boxplot() + theme_bw(26) + xlab("") + theme(legend.key = element_blank(), legend.position = "top", legend.key.size = unit(1.4, "cm")) + scale_fill_manual(name="",values=grey(c(0.95, 0.8, 0.5, 0.2)))

foo <- dev.off();