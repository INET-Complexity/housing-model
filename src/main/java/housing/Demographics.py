import random

from .housing.Household import Household

class Demographics:
    def __init__(self, Model):
        self.config = Model.config
        self.Model = Model

    # Adds newly "born" households to the model and removes households that "die".
    def step(self) -> None:
        # Birth: Add new households at a rate compatible with the age at birth distribution, the probability of
        # death dependent on age, and the target population
        nBirths = (int)(self.config.TARGET_POPULATION * data.Demographics.getBirthRate() + random.random())
        # Finally, add the households, with random ages drawn from the corresponding distribution
        while nBirths > 0:
            self.Model.households.add(Household())
            nBirths -= 1
        # Death: Kill households with a probability dependent on their age and organise inheritance
        for h in self.Model.households:
            pDeath = data.Demographics.probDeathGivenAge(h.getAge()) / self.config.constants.MONTHS_IN_YEAR
            if random.random() < pDeath:
                self.Model.households.remove(h)
                # Inheritance
                target = random.randint(0, len(self.Model.households) - 1)
                h.transferAllWealthTo(self.Model.households[target])
