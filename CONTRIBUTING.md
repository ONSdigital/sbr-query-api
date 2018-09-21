Contributing guidelines
=======================

### Git workflow

* We use git-flow - create a feature branch from `develop`, e.g. `feature/new-feature`
* Pull requests must contain a succinct, clear summary of what the user need is driving this feature change
* Ensure your branch contains logical atomic commits before sending a pull request - follow the [alphagov Git styleguide](https://github.com/alphagov/styleguides/blob/master/git.md)
* You may rebase your branch after feedback if it's to include relevant updates from the develop branch. We prefer a rebase here to a merge commit as we prefer a clean and straight history on develop with discrete merge commits for features

### Scala Style

Try to follow the [Scala Style Guidelines](http://docs.scala-lang.org/style/).

### Swagger Annotations

For any new API endpoints, make sure you annotate them in the controller.

The documentation for Swagger Annotations can be found [here](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X).


### Testing
As mentioned above, all pull request must have test. Use the applicable types(s).

#### Types
sbr-control-api tests are divided into three main categories;
* it - Integration Tests
    - Purpose: Tests the application's interaction and functioning with other components. 
* unit - Unit Testing
    - Purpose - Validate if intermediary operations and granular statements or functions react and perform as expected.
* server - Server/browser based Tests
    - Purpose - These tests directly test the application objects and features to observe the request and response - ensuring both are valid.
    - Typically in a Play framework environment this will include testing the controllers.