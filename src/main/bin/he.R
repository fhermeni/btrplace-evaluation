#!/usr/bin/env Rscript
args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
cnt <- read.table(args[1], header=T, sep="\t", na.strings=c("-"))
cnt$duration = cnt$computationDuration / 1000 + cnt$coreRP / 1000+ cnt$speRP / 1000
cnt = cnt[cnt$scenario == "he", c("duration","continuous")]
m = max(cnt$duration)
cnt$duration[is.na(cnt$duration)] <- 4000
conti = cnt[cnt$continuous == 1, ]
dist = cnt[cnt$continuous == 0, ]
colors <- c(grey(0.6), grey(0))
w=5
plot(ecdf(conti$duration), yaxt='n', xlim=c(8,m), col=grey(0), lty=1, do.points=FALSE, lwd=w, xlab="Duration (sec.)", ylab="Solved instances", main="", verticals = TRUE, panel.first=grid())
legend(8,1, c("discrete","continuous"), col=colors, cex=1, lty=1, lwd=w, border = FALSE, bty="n")
plot(ecdf(dist$duration), xlim=c(5,330), do.points=FALSE, lwd=w, col=grey(0.6), add=TRUE, verticals = TRUE)
axis(2,at=seq(0,1,0.2), labels=seq(0,100,20))
foo <- dev.off()