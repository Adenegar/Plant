# Requirements Statement
 
## How I've met the requirements

Documentation: Each file contains consistent javadocs and comments. UML diagrams have been generated for each class using PlantUML. ChatGPT (o3-mini-high) was used to create first versions of UML generation scripts and javadocs. 

Project:
- I have at least two plants running at a time.
- There are multiple workers operating per plant on different tasks.
- The final project is committed and pushed to Github.

## Challenges I faced

Initial setup:

For a while when I was setting up the first version of the simulation, I had to work through problems without being able to see the program run. In this intermediate state I had to try and figure out the problems rather than running the program and being able to clearly see the problems. 

Unexpected 
- Double fetching bug: Originally we fetched the orange two times in a row. I had to clarify and change part of the code.
- Orange initialization overhead and strange scaling: It seems that initializing oranges takes some overhead which makes the fetchers execute slower than we'd expect considering just the thread.sleep times for each task. Exploring further, when we scale up to many plants, the fetchers seem to execute faster than expected, perhaps suggesting a priority difference for instantiation compared to the other tasks. These factors resulted in a bit of unexpected behavior while testing. 

