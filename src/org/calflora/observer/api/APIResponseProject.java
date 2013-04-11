package org.calflora.observer.api;

import org.calflora.observer.model.Project;


public class APIResponseProject extends APIResponseBase {
	public Project data;
}

/*
orgId: 42,
projectId: "pr2",
center_lat: 37.52483,
center_lng: -122.409,
tilepackage: "https://www.calflora.org/tilep/YosemiteBaseCache.tpk",
tilepackageSize: 1367509,
plantlist: [
{
taxon: "Acacia dealbata",
common: "Silver wattle",
nstatus: "5",
lifeform: "Tree, Shrub",
CRN: "29",
family: "FABACEAE",
credit: "2008 Neal Kramer",
photoId: "0000-0000-0408-0757"
},
{
taxon: "Acacia melanoxylon",
common: "Blackwood acacia",
nstatus: "5",
lifeform: "Tree",
CRN: "36",
family: "FABACEAE",
credit: "2009 Neal Kramer",
photoId: "0000-0000-1009-2209"
}
]
}
*/