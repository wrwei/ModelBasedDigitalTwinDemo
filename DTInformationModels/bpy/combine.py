import typing
import bpy

# Begin of blender.stackexchange.com/questions/172559
def traverse_tree(node: bpy.types.Collection):
    yield node
    for subNode in node.children:
        yield from traverse_tree(subNode)
def parent_lookup(root: bpy.types.Collection) -> typing.Dict[str, bpy.types.Collection]:
    parent_table = {}
    for collection in traverse_tree(root):
        for subCollection in collection.children.keys():
            parent_table.setdefault(subCollection, collection)
    return parent_table
# End of online snippet

root = bpy.context.scene.collection
parent_cache : typing.Dict[str, bpy.types.Collection] = parent_lookup(root)

delete_list : typing.List[bpy.types.Collection] = []
for collection in traverse_tree(root):
    if collection.name.startswith("Segment"):
        continue
    if collection.name.startswith("Shared"):
        continue
    if collection.name.startswith("Environment"):
        continue
    
    if len(collection.objects) < 1:
        continue

    main = collection.objects[0]

    bpy.ops.object.select_all(action='DESELECT')

    bpy.ops.object.select_same_collection(collection=collection.name)
    bpy.context.view_layer.objects.active = main
    bpy.ops.object.join()

    main.name = collection.name.split('.')[0]

    collection.objects.unlink(main)
    parent_cache.get(collection.name).objects.link(main)
    
    delete_list.append(collection)

for collection in delete_list:
    if collection.name in bpy.data.collections.keys():
        bpy.data.collections.remove(collection)