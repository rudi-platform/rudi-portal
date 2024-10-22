# Rudi - Facet - RVA

Facette d'accès aux services Référentiel voie/adresse.

## Description

Cette facette a pour objectif de rechercher des addresses sur un ensemble de communes. 

Elle propose 3 implémentations :

- L'api RVA de Rennes métropole <a>https://api-rva.sig.rennesmetropole.fr</a>
- L'api Ban de l'état
- L'api OpenStreetMap

## Intégration dans un µs

- Rajouter la facette dans les dépendances du µs
- Créer un controlleur dans son µs qui utilise les services de la facette (**AddressService**)
- Surcharger les propriétés nécessaires pour l'implémentation retenue :
	* rudi.rva.implementation=osm|rva|ban
	
Chaque implémentation dispose d'une classe **XXXProperties** listant les propriétés configurables.


