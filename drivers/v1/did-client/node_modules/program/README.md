Installation
============

Global
------

    sudo npm install -g program
    echo ". prog.sh >> ~/.profile"


Local (recommended for development)
-----------------------------------

    ln -s $(pwd)/prog.js ~/bin/prog.js
    ln -s $(pwd)/prog.sh ~/bin/prog.sh
    echo ". prog.sh >> ~/.profile"
