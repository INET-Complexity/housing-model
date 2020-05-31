from typing import Any

class BinnedData(list):
    def __init__(self, firstBinMin: float, binWidth: float):
        self.firstBinMin = firstBinMin
        self.binWidth = binWidth

    def getSupportLowerBound(self) -> float:
        return self.firstBinMin

    def getSupportUpperBound(self) -> float:
        return self.firstBinMin + len(self) * self.binWidth

    def getBinWidth(self) -> float:
        return self.binWidth

    def getBinAt(self, val: float) -> Any:
        return self[int((val - self.firstBinMin) / self.binWidth)]

    def setBinWidth(self, width: float) -> None:
        self.binWidth = width

    def setFirstBinMin(self, _min: float) -> None:
        self.firstBinMin = _min
