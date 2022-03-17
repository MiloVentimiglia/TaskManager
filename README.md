# Task Manager

## Project Structure:

The traits and respective implementations are in the core package.
The data ADT are in the model file whereas acceptance tests are in the test folder.

## Implemented Features:

1. Add a process
2. Add a process – FIFO approach
3. Add a process – Priority based
4. List running processes
5. Kill/KillGroup/KillAll


## Design decisions:

1.  The Task Manager implementation is covariant. This means one can derive case classes by extending processes and
    have a queue of different tasks that extend the process trait. Extending inherently requires the implementation
    of group, id, timestamp, priority, and thus all the methods remain valid.

2.  Manager is not open for extension. By making it sealed we let the compiler know that only classes inside the same file
    as the declaration of Manager can inherit/implement it.

3.  Every operation (Ops) generates a new instance of the Manager class. Each object is a real world entity, and contains
    state and behavior. This, in turn, allows to easily store the state of the Manager (e.g. in a list) and 1) undo/do actions, and 2) recover in case of failure.
