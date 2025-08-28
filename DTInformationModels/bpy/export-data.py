import bpy
import mathutils
import typing
import json
import os

output : typing.List[dict] = []

for object in bpy.context.scene.objects.values():
    bpy.ops.object.select_all(action='DESELECT')

    dependency : typing.List[str] = object.users_collection[0].name.split('.')
    if len(dependency) < 1:
        continue

    entry : dict = {}
    entry['name'] = object.name

    untracked = False
    belongs : typing.List[str] = []
    if len(dependency) == 1:
        untracked = True
    else:
        belongs : typing.List[str] = dependency
        belongs.pop(0)

    entry['untracked'] = untracked
    entry['belongs'] = belongs
    entry['location'] = object.location.to_tuple()

    aabbCorners = [object.matrix_world @ mathutils.Vector(corner) for corner in object.bound_box]
    minVector = mathutils.Vector((
        min(aabbCorners, key=lambda corner: corner.x).x,
        min(aabbCorners, key=lambda corner: corner.y).y,
        min(aabbCorners, key=lambda corner: corner.z).z
    ))
    maxVector = mathutils.Vector((
        max(aabbCorners, key=lambda corner: corner.x).x,
        max(aabbCorners, key=lambda corner: corner.y).y,
        max(aabbCorners, key=lambda corner: corner.z).z
    ))

    entry['aabbMin'] = minVector.to_tuple()
    entry['aabbMax'] = maxVector.to_tuple()

    surface = 0
    for polygon in object.data.polygons:
        surface += polygon.area
    entry['surface'] = surface

    output.append(entry)


json_text = json.dumps({"entries": output}, indent=2)
json_path = os.path.join(os.getcwd(), 'reference.json')
with open(file=json_path, mode='w') as outputFile:
    outputFile.write(json_text)
