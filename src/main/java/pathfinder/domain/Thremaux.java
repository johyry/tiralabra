/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder.domain;

import java.util.Random;

/**
 *
 * @author johyry
 */
public class Thremaux {

    private int[][] maze;
    private Coordinates start;
    private Coordinates goal;
    public int[][] marked; // public for testing purposes
    private Coordinates[][] previousPoint;

    public Thremaux(int[][] maze, Coordinates start, Coordinates goal) {
        this.maze = maze;
        this.start = start;
        this.goal = goal;
        this.marked = new int[maze.length][maze[0].length];
        table2DInitZeros(marked);
        this.previousPoint = new Coordinates[maze.length][maze[0].length];
        
    }

    public void init() {
        
    }

    public int search() {
        // 1. Liiku Taulukossa DONE
        // 2. Merkkaa reitti aina kun liikut sillä DONE
        //     - Risteyksiä ei ikinä merkata
        // 3. Tarkista, oletko maalissa NOT
        // 4. Älä koskaan mene reitille jossa on kaksi merkkiä DONE
        // 5. Kun saavut risteykseen, jossa muita reittejä kun tuloreitti DONE
        //    ei ole merkitty, valitse satunnainen reitti 
        // 6. Muuten:
        //     - Jos reitillä josta saavuit on vain yksi merkki palaa sinne DONE
        //     - Jos kaksi, valitse reitti jossa on vähiten merkkejä, DONE
        //       jos yhtä paljon, valitse satunnaisesti

//        Huomioita:
//            - Pidä kirjaa mistä saavuit

        // Arvoksi tulee 1, jos reitti löytyi, ja 0, jos ei
        int foundRoute = -1;
        
        int calculationsDone = 0;

        // Aloituspiste
        Coordinates current = start;
        // Merkataan lähtö käydyksi
        marked[start.getY()][start.getX()] = 1;
        // Lähdön edellinen piste on lähtö
        previousPoint[start.getY()][start.getX()] = start;

        while (true) {
            int x = current.getX();
            int y = current.getY();
            
            calculationsDone++;
            
//            System.out.println("Round: " + calculationsDone + " x: " + x + " y: " + y);

            // Tarkistetaan, ollaanko maalissa
            if (x == goal.getX() && y == goal.getY()) {
                foundRoute = 1;
                break;
            }
            
            // Tarkistetaan, ollaanko palattu lähtöön, ja siellä on kaksi merkkiä
            // -> Labyrintistä ei ole reittiä ulos
            if (x == start.getX() && y == start.getY() && marked[start.getY()][start.getX()] == 2) {
                foundRoute = 0;
                break;
            } 
            
            // Edellinen piste
            Coordinates previous = previousPoint[y][x];

            // taulukko, jossa on tiedot onko risteys ([0]), ja mitkä suunnat mahdollisia([1-4])
            int[] junctionTable = howManyRouteOptions(current, previous);
            // Jos on risteys
            if (junctionTable[0] > 1) {

                // Onko muita reittejä kun tuloreitti merkitty
                if (!otherOptionsThanPreviousMarked(current, previous)) {
                    // Jos ei, valitse satunnainen reitti mahdollisista
                    Coordinates next = chooseRandomRoute(junctionTable, current, randomInt(junctionTable[0]));
                    previousPoint[next.getY()][next.getX()] = current;
                    current = next;
                    continue;

                    // Jos muita reittejä on merkitty
                } else {
                    // Jos reitillä josta saavuit on vain yksi merkki, palaa sinne
                    if (marked[previous.getY()][previous.getX()] == 1) {
                        previousPoint[previous.getY()][previous.getX()] = current;
                        current = previous;
                        continue;
                    }
                    // Jos kaksi, valitse reitti jossa on vähiten merkkejä, jos yhtä vähän, valitse satunnaisesti
                    Coordinates next = chooseOptionWithLowestMarks(junctionTable, current);
                    previousPoint[next.getY()][next.getX()] = current;
                    current = next;

                }

                // Jos ei risteys
            } else {
                // Liiku eteenpäin, tai jos umpikuja, taaksepäin
                Coordinates next = moveForward(junctionTable, current, previous);
                previousPoint[next.getY()][next.getX()] = current;
                // Merkitään piste, kun siitä lähdetään. Risteyksiä ei ikinä merkata.
                marked[y][x] = marked[y][x]+1;
                current = next;
            }
        }
        
        System.out.println("Rounds done: " + calculationsDone);
        return foundRoute;

    }

    /**
     * Liikkuu reitillä eteenpäin, ja jos edessä umpikuja, taaksepäin
     *
     * @return seuraavan pisteen Coordinates -olio
     */
    public Coordinates moveForward(int[] junctionTable, Coordinates current, Coordinates previous) {
        int x = current.getX();
        int y = current.getY();
        
        if (junctionTable[1] == 1) {
            return new Coordinates(y - 1, x);

        }

        if (junctionTable[2] == 1) {
            return new Coordinates(y, x + 1);

        }

        if (junctionTable[3] == 1) {
            return new Coordinates(y + 1, x);

        }

        if (junctionTable[4] == 1) {
            return new Coordinates(y, x - 1);
        }
        // Jos umpikujassa, umpikujaan laitetaan merkki
        marked[y][x] = marked[y][x]+1;
        return previous;
    }

    /**
     * Valitsee satunnaisesti reitin vaihtoehdoista, joilla on yhtä vähän
     * merkkejä reitillä
     *
     * @return Coordinates -olio
     */
    public Coordinates chooseOptionWithLowestMarks(int[] junctionTable, Coordinates current) {

        // Mikä on minimi merkkimäärä vaihtoehdoilla
        // Kuinka monta niitä on
        int lowestMarks = 3;
        int amount = 0;
        int marks = 0;

        int x = current.getX();
        int y = current.getY();

        if (junctionTable[1] == 1) {
            marks = marked[y - 1][x];
            if (marks < lowestMarks) {
                lowestMarks = marks;
                amount = 1;
            } else if (marks == lowestMarks) {
                amount++;
            }
        }

        if (junctionTable[2] == 1) {
            marks = marked[y][x + 1];
            if (marks < lowestMarks) {
                lowestMarks = marks;
                amount = 1;
            } else if (marks == lowestMarks) {
                amount++;
            }

        }

        if (junctionTable[3] == 1) {
            marks = marked[y + 1][x];
            if (marks < lowestMarks) {
                lowestMarks = marks;
                amount = 1;
            } else if (marks == lowestMarks) {
                amount++;
            }

        }

        if (junctionTable[4] == 1) {
            marks = marked[y][x - 1];
            if (marks < lowestMarks) {
                lowestMarks = marks;
                amount = 1;
            } else if (marks == lowestMarks) {
                amount++;
            }

        }

        // Valitaan satunnaisesti vaihtoehdoista jolla on yhtä vähän merkkejä
        int randomNr = randomInt(amount);

        if (junctionTable[1] == 1) {
            marks = marked[y - 1][x];
            if (marks == lowestMarks) {
                if (randomNr == 0) {
                    return new Coordinates(y - 1, x);
                }
                randomNr--;
            }
        }

        if (junctionTable[2] == 1) {
            marks = marked[y][x + 1];
            if (marks == lowestMarks) {
                if (randomNr == 0) {
                    return new Coordinates(y, x + 1);
                }
                randomNr--;
            }

        }

        if (junctionTable[3] == 1) {
            marks = marked[y + 1][x];
            if (marks == lowestMarks) {
                if (randomNr == 0) {
                    return new Coordinates(y + 1, x);
                }
                randomNr--;
            }

        }

        if (junctionTable[4] == 1) {
            marks = marked[y][x - 1];
            if (marks == lowestMarks) {
                if (randomNr == 0) {
                    return new Coordinates(y, x - 1);
                }
            }
        }
        return null;
    }

    /**
     * Arpoo satunnaisen numeron väliltä 0-possibleOptions
     *
     * @return satunnainen int 0-possibleOptions väliltä
     */
    public int randomInt(int possibleOptions) {
        Random random = new Random();
        int randomNr = random.nextInt(possibleOptions);
        return randomNr;
    }

    /**
     * Valitsee mahdollisista vaihtoehdoista risteyksessä satunnaisen ja
     * palauttaa sen
     *
     * @return Coordinates -olion, missä on valitun reitin ensimmäisen pisteen
     * koordinaatit
     */
    public Coordinates chooseRandomRoute(int[] junctionTable, Coordinates current, int randomNr) {
        int amount = junctionTable[0];

        int x = current.getX();
        int y = current.getY();

        if (junctionTable[1] == 1) {
            if (randomNr == 0) {
                return new Coordinates(y - 1, x);
            }
            randomNr--;
        }

        if (junctionTable[2] == 1) {
            if (randomNr == 0) {
                return new Coordinates(y, x + 1);
            }
            randomNr--;
        }

        if (junctionTable[3] == 1) {
            if (randomNr == 0) {
                return new Coordinates(y + 1, x);
            }
            randomNr--;
        }

        if (junctionTable[4] == 1) {
            if (randomNr == 0) {
                return new Coordinates(y, x - 1);
            }
        }
        return null;
    }

    /**
     * Tarkistaa, onko risteyksessä muut mahdolliset reitit merkattu kuin se
     * mistä tultiin
     *
     * @return true jos on, false jos ei
     */
    public boolean otherOptionsThanPreviousMarked(Coordinates current, Coordinates previous) {
        int x = current.getX();
        int y = current.getY();

        int amount = 0;

        // Ylös
        if (y != 0) {
            if (previous.getY() != y - 1 && marked[y - 1][x] > 0) {
                amount++;
            }
        }

        // Oikea
        if (previous.getX() != x + 1 && marked[y][x + 1] > 0) {
            amount++;
        }

        // Alas
        if (y != maze.length - 1) {
            if (previous.getY() != y + 1 && marked[y + 1][x] > 0) {
                amount++;
            }
        }

        // Vasen
        if (previous.getX() != x - 1 && marked[y][x - 1] > 0) {
            amount++;
        }

        return amount > 0;
    }

    /**
     * Laskee 1) mahdollisten suuntien määrän kyseisestä pisteestä kohtaan
     * taulukko[0] 2) minne suuntaan voi pisteestä liikkua, 1 == on reitti, 0 ==
     * ei ole reittiä [1] = ylös [2] = oikealle [3] = alas [4] = vasemmalle
     *
     *
     * @return taulukon, taulukko[0] = mahdollisten suuntien määrän, table[1]
     * ylös, table[2] oikealle, table[3] alas, table[4] vasemmalle
     */
    public int[] howManyRouteOptions(Coordinates current, Coordinates previous) {
        int[] table = new int[5];
        for (int i = 0; i < table.length; i++) {
            table[i] = 0;
        }

        int x = current.getX();
        int y = current.getY();

        int options = 0;

        // Ylös
        if (y != 0) {
            if (previous.getY() != y - 1 && maze[y - 1][x] == 1) {
                options++;
                table[1] = 1;
            }
        }

        // Oikea
        if (previous.getX() != x + 1 && maze[y][x + 1] == 1) {
            options++;
            table[2] = 1;
        }

        // Alas
        if (y != maze.length - 1) {
            if (previous.getY() != y + 1 && maze[y + 1][x] == 1) {
                options++;
                table[3] = 1;
            }
        }

        // Vasen
        if (previous.getX() != x - 1 && maze[y][x - 1] == 1) {
            options++;
            table[4] = 1;
        }

        table[0] = options;

        return table;
    }

    private void table2DInitZeros(int[][] table) {
        for (int y = 0; y < table.length; y++) {
            for (int x = 0; x < table[0].length; x++) {
                table[y][x] = 0;
            }
        }
    }
}
