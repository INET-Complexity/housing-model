[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a740a85350b54e49b49dd84157f30fac)](https://www.codacy.com/app/EconomicSL/housing-model?utm_source=github.com&utm_medium=referral&utm_content=EconomicSL/housing-model&utm_campaign=badger)

Agent Based Model of the UK Housing Market
==========================================

This is an agent based model of the UK housing market written by the Institute of New Economic Thinking at Oxford university, in collaboration with The Bank of England. It is intended for use as a tool for informing central bank regulation policy.

The model incorporates owner-occupiers, renters, buy-to-let investors, a housing market, a rental market, banks, a central bank and a government. A more detailed description of the model can be found in the HousingModelBoE.pdf file, and the Javadoc can be found in the doc directory.

The model uses the MASON library (http://cs.gmu.edu/~eclab/projects/mason/). The main function is in ModelGUI, which interfaces with the MASON gui. The root object of the model is Model.
