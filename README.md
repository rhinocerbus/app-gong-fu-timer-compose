# Gong Fu Brewing Timer

## Description
App for managing a Gong Fu tea brewing session, which consists of several, short steepings after a longer initial steep.
Timings for these steepings can vary with the type of tea, so presets are provided along with some standard baseline values.

The core feature set was completed in around 4 hours, including multiple ui design passes as well as adding more features to private libraries for shared components.

## Tech stack
Compose, coroutines, Hilt. 

## Additional todo:
- [x] add a countdown for the round starting to give time to start pouring water after click
- [x] keep screen on when timer is running
- [ ] have screen bg flash some color for regaining attention when steeping is done
- [ ] add a foreground service for hosting a notification for better multitasking 
- [ ] could use a sound or haptic when timer is done
An additional layer of features could be added, including a local/remote db to store settings for tea types provided by the user, with local storage for last tea selected, plus tracking most-brewed teas. Since this is a personal-use tool, I don't particularly care to have that feature set at the momnet.
