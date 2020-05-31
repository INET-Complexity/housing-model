class CollectorBase:
    def __init__(self):
        self.active = False

    def isActive(self):
        return self.active

    def setActive(self, active: bool):
        self.active = active
