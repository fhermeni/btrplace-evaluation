#!/usr/bin/env Rscript
args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0),cex=1.3)
cnt <- read.table(args[1], header=T, sep="\t", na.strings=c("-"))
cnt$duration = cnt$computationDuration / 1000 + cnt$coreRP / 1000+ cnt$speRP / 1000
cnt = cnt[cnt$scenario == 0, c("duration","continuous")]
m = max(cnt$duration)
cat("Maximum duration for discrete/ve ", max(cnt$duration[cnt$continuous == 0]), "\n")
cnt$duration[is.na(cnt$duration)] <- 4000
conti = cnt[cnt$continuous == 1, ]
dist = cnt[cnt$continuous == 0, ]
oh = (1 - mean(dist$duration) / mean(conti$duration)) * 100
cat("Computational overhead for continuous/ve: " , oh, "%\n")

colors <- c(grey(0.8), grey(0))
plot(ecdf(conti$duration), yaxt='n', xlim=c(5,330), col=grey(0), lty=1, log="x", do.points=FALSE, lwd=5, xlab="Duration (sec.)", ylab="Solved instances", main="", verticals = TRUE, panel.first=grid())
legend(40,0.3, c("discrete","continuous"), col=colors, cex=1, lty=1, lwd=5, border = FALSE, bty="n")
plot(ecdf(dist$duration), xlim=c(5,330), do.points=FALSE, lwd=5, col=grey(0.8), add=TRUE, verticals = TRUE)
axis(2,at=seq(0,1,0.2), labels=seq(0,100,20))
foo <- dev.off()