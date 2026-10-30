package it.unife.volley.backend.config;

/** Nomi e cognomi usati per generare tesserati e staff dei dati dimostrativi. */
final class NomiDemo {

    private NomiDemo() {
    }

    /** Nomi maschili: all'interno della stessa squadra non si ripetono. */
    static final String[] NOMI = {
            "Lorenzo", "Matteo", "Alessandro", "Francesco", "Andrea", "Riccardo", "Tommaso", "Gabriele",
            "Davide", "Federico", "Nicola", "Giacomo", "Pietro", "Filippo", "Edoardo", "Luca",
            "Michele", "Samuele", "Jacopo", "Leonardo", "Mattia", "Daniele", "Emanuele", "Simone",
            "Marco", "Stefano", "Paolo", "Giovanni", "Antonio", "Giulio", "Enrico", "Alberto",
            "Fabio", "Massimo", "Roberto", "Claudio", "Diego", "Cristian", "Manuel", "Alex",
            "Elia", "Kevin", "Mirko", "Ivan", "Omar", "Thomas", "Denis",
            "Raffaele", "Vittorio", "Valerio", "Ettore", "Dario", "Sergio", "Fabrizio",
            "Maurizio", "Luigi", "Carlo", "Franco", "Angelo", "Mario", "Giorgio",
            "Umberto", "Ludovico", "Achille", "Cesare", "Saverio", "Ruben", "Alessio", "Gianluca",
            "Gianmarco", "Pierluigi", "Lucio", "Renato", "Silvio", "Tiziano", "Walter"};

    /** Cognomi: ognuno viene assegnato a una sola persona in tutto il database. */
    static final String[] COGNOMI = {
            "Russo", "Ferrari", "Esposito", "Bianco", "Romano", "Colombo", "Ricci", "Marino",
            "Greco", "Gallo", "Conti", "De Luca", "Mancini", "Giordano",
            "Rizzo", "Lombardi", "Moretti", "Barbieri", "Fontana", "Santoro", "Mariani", "Rinaldi",
            "Caruso", "Ferrara", "Galli", "Martini", "Leone", "Longo", "Gentile", "Martinelli",
            "Vitale", "Lombardo", "Coppola", "De Santis", "D'Angelo", "Marchetti", "Parisi", "Villa",
            "Conte", "Ferraro", "Ferri", "Fabbri", "Bianchi", "Marini", "Grasso", "Valentini",
            "Messina", "Sala", "De Angelis", "Gatti", "Pellegrini", "Palumbo", "Sanna", "Farina",
            "Rossetti", "Cattaneo", "Bernardi", "Fiore", "Testa", "Silvestri", "Monti", "Ruggiero",
            "Bellini", "Orlando", "Pagano", "Morelli", "Cristiani", "Amato", "Guerra", "Mazza",
            "Montanari", "Basile", "Donati", "Benedetti", "Marchi", "Riva", "Cirillo", "Piras",
            "Neri", "Fumagalli", "Gregori", "Carbone", "Sorrentino", "Melis", "Pace", "Bertolini",
            "Ruggeri", "Pellegrino", "Ceccarelli", "Battaglia", "Fadda", "Zanetti", "Pastore", "Bellucci",
            "Mele", "Calabrese", "Barone", "Zanella", "Castelli", "Lazzari", "Bassi", "Riccardi",
            "Ferretti", "Grimaldi", "Tosi", "Morandi", "Mattei", "Cavalli", "Mazzoni", "Rota",
            "Negri", "Marras", "Guidi", "Ceccato", "Pepe", "Valenti", "Fabbro", "Poli",
            "Caputo", "Cocco", "Milani", "Mancuso", "Orsini", "Gualtieri", "Ricciardi", "Bonetti",
            "Rocca", "Rossini", "Brambilla", "Bruni", "Mantovani", "Rossetto", "Piazza", "Ferrero",
            "Piccolo", "Tedesco", "Longhi", "Fusco", "Campana", "Gori", "Zanni", "Nardi",
            "Bonelli", "Pozzi", "Casadei", "Lodi", "Righi", "Vecchi", "Pancaldi", "Golinelli",
            "Malaguti", "Benassi", "Bertoni", "Rambaldi", "Cavazza", "Zanotti", "Bergonzoni", "Vignoli",
            "Montanaro", "Gherardi", "Baraldi", "Borghi", "Corradini", "Zaccarelli", "Bonfiglioli", "Tassinari",
            "Zucchini", "Pasini", "Sabbatini", "Bertozzi", "Sassatelli", "Mingardi", "Fiorini", "Lugli",
            "Bonacini", "Gozzi", "Venturi", "Ghelfi", "Calzolari", "Bagni", "Burani", "Corsini",
            "Ferioli", "Gamberini", "Grandi", "Lambertini", "Magnani", "Manzini", "Masetti", "Melotti",
            "Monari", "Nanni", "Nannini", "Orlandini", "Pagani", "Panzacchi", "Pirazzoli", "Pizzi",
            "Poggi", "Prati", "Raimondi", "Ravaglia", "Rinaldini", "Rubini", "Salvioli", "Santini",
            "Scarani", "Serra", "Simoni", "Soldati", "Spinelli", "Stagni", "Tarozzi", "Tinti",
            "Tommasini", "Trombetti", "Turrini", "Uguzzoni", "Vaccari", "Valli", "Venturelli", "Vezzali",
            "Vincenzi", "Zambelli", "Zanardi", "Zanasi", "Zappoli", "Zarri", "Zecchi", "Zoboli",
            "Alberti", "Alessandri", "Amadori", "Andreoli", "Angelini", "Ansaloni", "Ardizzoni", "Armaroli",
            "Baldini", "Balboni", "Bandini", "Bartolini", "Bastia", "Bazzani", "Belletti", "Benini",
            "Bernabei", "Berselli", "Bertelli", "Biagini", "Bigi", "Bisi", "Bocchi", "Bolognesi",
            "Bompani", "Bonora", "Borelli", "Bortolotti", "Bosi", "Bottazzi", "Bovina", "Brighenti",
            "Brunelli", "Buldrini", "Busi", "Cagnoli", "Calanchi", "Callegari", "Camorani", "Canè",
            "Cantelli", "Capelli", "Capucci", "Carletti", "Casali", "Casari", "Castaldini", "Cavicchi",
            "Cenacchi", "Cervellati", "Chiarini", "Cocchi", "Collina", "Comastri", "Contri", "Corticelli",
            "Costantini", "Cremonini", "Dall'Olio", "Degli Esposti", "Dondi", "Draghetti", "Ercolani", "Fabbiani",
            "Facchini", "Fantuzzi", "Farnè", "Fava", "Felicori", "Fiorentini", "Folli", "Forni",
            "Franchini", "Frabetti", "Fregni", "Galletti", "Gamberoni", "Garagnani", "Gardini", "Gasparini",
            "Ghedini", "Ghetti", "Giacomelli", "Gibertoni", "Gnudi", "Govoni", "Grilli",
            "Guizzardi", "Lanzoni", "Lenzi", "Lipparini", "Maccaferri", "Macchiavelli", "Maestri",
            "Malossi", "Marani", "Marchesini", "Marzocchi", "Mascagni", "Mazzetti", "Medici", "Merighi",
            "Minarelli", "Montecchi", "Morara", "Musiani", "Nascetti", "Nerozzi", "Nicoli",
            "Nicolini", "Orsi", "Pallotti", "Pasquali", "Pederzoli", "Pedrini", "Pelloni",
            "Perdisa", "Pesci", "Piana", "Pinardi", "Pradelli", "Preti", "Quadri", "Rabitti",
            "Ragazzi", "Reggiani", "Ricchi", "Roncarati", "Rondelli", "Rubbini", "Sabattini", "Santi",
            "Sarti", "Scagliarini", "Selleri", "Sermasi", "Sgarbi", "Soverini", "Stanzani", "Stefanelli",
            "Sverzellati", "Taddia", "Tagliavini", "Tamburini", "Tartari", "Tedeschi", "Testoni", "Tonelli",
            "Trebbi", "Ugolini", "Vandelli", "Vannini", "Verardi", "Zacchini", "Zambonelli",
            "Zamboni", "Zanarini", "Zuffi"};
}
